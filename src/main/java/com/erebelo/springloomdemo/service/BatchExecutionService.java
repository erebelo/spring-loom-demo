package com.erebelo.springloomdemo.service;

import com.erebelo.springloomdemo.domain.enumertion.BatchStatus;
import com.erebelo.springloomdemo.domain.model.BatchExecution;
import com.erebelo.springloomdemo.domain.model.BatchFailedRecord;
import com.erebelo.springloomdemo.domain.model.WriteContext;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchExecutionService {

    private final MongoTemplate mongoTemplate;

    public void create(String executionId, String processor) {
        BatchExecution execution = BatchExecution.builder().id(executionId).processor(processor)
                .status(BatchStatus.PENDING).startedAt(LocalDateTime.now(ZoneOffset.UTC)).successes(0).failures(0)
                .build();

        mongoTemplate.insert(execution);
    }

    public void markRunning(String executionId) {
        Query query = Query.query(Criteria.where("_id").is(executionId));

        Update update = new Update().set("status", BatchStatus.RUNNING);

        mongoTemplate.updateFirst(query, update, BatchExecution.class);
    }

    public void markCompleted(String executionId, WriteContext writeContext) {
        Query query = Query.query(Criteria.where("_id").is(executionId));

        Update update = new Update().set("status", BatchStatus.COMPLETED)
                .set("completedAt", LocalDateTime.now(ZoneOffset.UTC))
                .set("successes", Math.toIntExact(writeContext.getSuccessCount().get()))
                .set("failures", writeContext.getErrors().size());

        mongoTemplate.updateFirst(query, update, BatchExecution.class);
    }

    public void markFailed(String executionId, WriteContext writeContext, Exception ex) {
        Query query = Query.query(Criteria.where("_id").is(executionId));

        Update update = new Update().set("status", BatchStatus.FAILED)
                .set("completedAt", LocalDateTime.now(ZoneOffset.UTC))
                .set("successes", Math.toIntExact(writeContext.getSuccessCount().get()))
                .set("failures", writeContext.getErrors().size()).set("exceptionMessage", ex.getMessage())
                .set("stackTrace", ExceptionUtils.getStackTrace(ex));

        mongoTemplate.updateFirst(query, update, BatchExecution.class);
    }

    public void saveFailedRecords(String executionId, String processor, WriteContext writeContext) {
        if (writeContext.getErrors().isEmpty()) {
            return;
        }

        List<BatchFailedRecord> failedRecords = writeContext.getErrors().stream()
                .map(error -> BatchFailedRecord.builder().executionId(executionId).processor(processor)
                        .exceptionMessage(error.exception().getMessage())
                        .stackTrace(ExceptionUtils.getStackTrace(error.exception())).metadata(error.item()).build())
                .toList();

        mongoTemplate.insertAll(failedRecords);
    }
}
