package com.erebelo.springloomdemo.service;

import com.erebelo.springloomdemo.exception.model.ConflictException;
import com.erebelo.springloomdemo.exception.model.NotFoundException;
import com.erebelo.springloomdemo.model.dto.WriteContext;
import com.erebelo.springloomdemo.model.entity.BatchExecution;
import com.erebelo.springloomdemo.model.entity.BatchFailedRecord;
import com.erebelo.springloomdemo.model.enums.BatchStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchExecutionService {

    private final MongoTemplate mongoTemplate;

    public void startExecution(String executionId, String processor, Duration staleTimeout,
            Predicate<String> isExecutionManaged) {
        Query query = Query.query(Criteria.where("processor").is(processor).and("status").is(BatchStatus.RUNNING));

        BatchExecution execution = mongoTemplate.findOne(query, BatchExecution.class);

        if (execution != null) {
            recoverStaleRunningExecution(execution, processor, staleTimeout, isExecutionManaged);
        }

        BatchExecution newExecution = BatchExecution.builder().id(executionId).processor(processor)
                .status(BatchStatus.RUNNING).startedAt(Instant.now()).successes(0).failures(0).build();

        mongoTemplate.insert(newExecution);
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

    public void markCancelled(String executionId, WriteContext writeContext) {
        BatchExecution execution = findBatchExecutionById(executionId);

        execution.setStatus(BatchStatus.CANCELLED);
        execution.setCompletedAt(Instant.now());
        execution.setSuccesses(execution.getSuccesses() + (int) writeContext.getSuccessCount().get());
        execution.setFailures(execution.getFailures() + writeContext.getErrors().size());

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

    private void recoverStaleRunningExecution(BatchExecution execution, String processor, Duration staleTimeout,
            Predicate<String> isExecutionManaged) {
        Instant now = Instant.now();

        if (!isExecutionManaged.test(execution.getId())) {
            markRecoveredAsFailed(execution, now,
                    "Execution automatically marked as FAILED because it is no longer managed by the application. "
                            + "This typically indicates the application restarted or terminated unexpectedly.");
            return;
        }

        Instant lastActivity = execution.getLastCheckpointAt() != null
                ? execution.getLastCheckpointAt()
                : execution.getStartedAt();

        if (lastActivity.plus(staleTimeout).isAfter(now)) {
            throw new ConflictException("A batch execution is already in progress for processor: " + processor);
        }

        markRecoveredAsFailed(execution, now,
                ("Execution automatically marked as FAILED because no checkpoint was recorded for longer than the "
                        + "configured stale timeout (%d minutes).").formatted(staleTimeout.toMinutes()));
    }

    private void markRecoveredAsFailed(BatchExecution execution, Instant completedAt, String exceptionMessage) {
        execution.setStatus(BatchStatus.FAILED);
        execution.setCompletedAt(completedAt);
        execution.setExceptionMessage(exceptionMessage);

        mongoTemplate.save(execution);
    }

    private BatchExecution findBatchExecutionById(String executionId) {
        BatchExecution execution = mongoTemplate.findById(executionId, BatchExecution.class);

        if (execution == null) {
            throw new NotFoundException("Batch execution not found: " + executionId);
        }

        return execution;
    }
}
