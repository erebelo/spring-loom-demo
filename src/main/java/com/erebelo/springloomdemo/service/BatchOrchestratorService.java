package com.erebelo.springloomdemo.service;

import com.erebelo.springloomdemo.domain.dto.WriteResultDto;
import com.erebelo.springloomdemo.service.csv.CsvReaderService;
import com.erebelo.springloomdemo.service.loom.LoomService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BatchOrchestratorService {

    private final ExecutorService batchExecutor;
    private final BatchExecutionService batchExecutionService;
    private final CsvReaderService csvReaderService;
    private final LoomService loomService;

    public BatchOrchestratorService(@Qualifier("batchExecutor") ExecutorService batchExecutor,
            BatchExecutionService batchExecutionService, CsvReaderService csvReaderService, LoomService loomService) {
        this.batchExecutor = batchExecutor;
        this.batchExecutionService = batchExecutionService;
        this.csvReaderService = csvReaderService;
        this.loomService = loomService;
    }

    /**
     * Starts a new batch execution asynchronously and immediately returns an
     * executionId that can be used to query the execution status.
     * <p>
     * The actual processing runs in a separate Virtual Thread, allowing the HTTP
     * request to return immediately without waiting for the batch to finish.
     */
    public <T> String process(BatchContext<T> context) {
        String executionId = "bulk-exec-" + UUID.randomUUID().toString().substring(0, 15);

        batchExecutionService.create(executionId);
        batchExecutor.execute(() -> executeBatch(executionId, context));

        return executionId;
    }

    /**
     * Executes the batch in the background.
     * <p>
     * Steps: 1. Mark execution as RUNNING. 2. Read and map the CSV into DTOs. 3.
     * Process records concurrently using LoomService. 4. Update the execution
     * status when finished.
     */
    private <T> void executeBatch(String executionId, BatchContext<T> context) {

        try {
            batchExecutionService.markRunning(executionId);

            List<T> records = csvReaderService.read(context.path(), context.mapper());
            WriteResultDto writeResult = loomService.write(records, context.persistFunction(),
                    context.recordIdExtractor());

            batchExecutionService.markCompleted(executionId, writeResult);
        } catch (Exception ex) {
            batchExecutionService.markFailed(executionId, ex);
        }
    }
}
