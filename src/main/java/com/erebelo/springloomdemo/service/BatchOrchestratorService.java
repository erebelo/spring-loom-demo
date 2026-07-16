package com.erebelo.springloomdemo.service;

import com.erebelo.springloomdemo.domain.model.WriteContext;
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

        batchExecutionService.create(executionId, context.processor());
        batchExecutor.execute(() -> executeBatch(executionId, context));

        return executionId;
    }

    /**
     * Executes the batch asynchronously.
     * <p>
     * If an unexpected failure occurs, the accumulated WriteContext preserves the
     * number of successful records and all record-level failures processed before
     * the interruption.
     */
    private <T> void executeBatch(String executionId, BatchContext<T> context) {
        WriteContext writeContext = new WriteContext();

        try {
            batchExecutionService.markRunning(executionId);

            List<T> records = csvReaderService.read(context.resource(), context.mapper());
            loomService.write(records, context.persistFunction(), context.recordIdExtractor(), writeContext);

            batchExecutionService.saveFailedRecords(executionId, context.processor(), writeContext);
            batchExecutionService.markCompleted(executionId, writeContext);
        } catch (Exception ex) {
            // Persist any partial failures accumulated before the batch stopped.
            batchExecutionService.saveFailedRecords(executionId, context.processor(), writeContext);
            batchExecutionService.markFailed(executionId, writeContext, ex);
        }
    }
}
