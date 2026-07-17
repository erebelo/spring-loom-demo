package com.erebelo.springloomdemo.service;

import com.erebelo.springloomdemo.domain.enumertion.BatchStatus;
import com.erebelo.springloomdemo.domain.model.BatchExecution;
import com.erebelo.springloomdemo.domain.model.BatchFailedRecord;
import com.erebelo.springloomdemo.domain.model.WriteContext;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchExecutionService {

    private final MongoTemplate mongoTemplate;

    public void createExecution(String executionId, String processor) {
        BatchExecution execution = BatchExecution.builder().id(executionId).processor(processor)
                .status(BatchStatus.PENDING).startedAt(Instant.now()).successes(0).failures(0).build();

        mongoTemplate.insert(execution);
    }

    public void markRunning(String executionId) {
        BatchExecution execution = findBatchExecutionById(executionId);

        execution.setStatus(BatchStatus.RUNNING);

        mongoTemplate.save(execution);
    }

    public void checkpoint(String executionId, WriteContext writeContext) {
        BatchExecution execution = findBatchExecutionById(executionId);

        execution.setLastCheckpointAt(Instant.now());
        execution.setSuccesses(execution.getSuccesses() + (int) writeContext.getSuccessCount().get());
        execution.setFailures(execution.getFailures() + writeContext.getErrors().size());

        mongoTemplate.save(execution);
    }

    public void markCompleted(String executionId) {
        BatchExecution execution = findBatchExecutionById(executionId);

        execution.setStatus(BatchStatus.COMPLETED);
        execution.setCompletedAt(Instant.now());

        mongoTemplate.save(execution);
    }

    public void markFailed(String executionId, WriteContext writeContext, Exception ex) {
        BatchExecution execution = findBatchExecutionById(executionId);

        execution.setStatus(BatchStatus.FAILED);
        execution.setCompletedAt(Instant.now());
        execution.setSuccesses(execution.getSuccesses() + (int) writeContext.getSuccessCount().get());
        execution.setFailures(execution.getFailures() + writeContext.getErrors().size());
        execution.setExceptionMessage(ex.getMessage());
        execution.setStackTrace(ExceptionUtils.getStackTrace(ex));

        mongoTemplate.save(execution);
    }

    public void saveFailedRecords(String executionId, String processor, WriteContext writeContext) {
        if (writeContext == null || writeContext.getErrors().isEmpty()) {
            return;
        }

        List<BatchFailedRecord> failedRecords = writeContext.getErrors().stream()
                .map(error -> BatchFailedRecord.builder().executionId(executionId).processor(processor)
                        .exceptionMessage(error.exception().getMessage())
                        .stackTrace(ExceptionUtils.getStackTrace(error.exception())).metadata(error.item()).build())
                .toList();

        mongoTemplate.insertAll(failedRecords);
    }

    private BatchExecution findBatchExecutionById(String executionId) {
        BatchExecution execution = mongoTemplate.findById(executionId, BatchExecution.class);

        if (execution == null) {
            throw new IllegalStateException("Batch execution not found: " + executionId);
        }

        return execution;
    }
}
