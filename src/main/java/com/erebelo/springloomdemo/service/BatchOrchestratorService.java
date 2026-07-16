package com.erebelo.springloomdemo.service;

import com.erebelo.springloomdemo.domain.model.WriteContext;
import com.erebelo.springloomdemo.service.csv.CsvBatchReader;
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
        String executionId = "bulk-exec-" + UUID.randomUUID().toString().substring(0, 18);

        batchExecutionService.createExecution(executionId, context.processor());
        batchExecutor.execute(() -> executeBatch(executionId, context));

        return executionId;
    }

    /**
     * Executes the batch asynchronously by processing the CSV in chunks.
     * <p>
     * A checkpoint is persisted after each chunk so progress, successful records,
     * and failed records are preserved even if the batch terminates unexpectedly.
     */
    private <T> void executeBatch(String executionId, BatchContext<T> context) {
        batchExecutionService.markRunning(executionId);
        WriteContext writeContext = new WriteContext();

        try (CsvBatchReader<T> batches = csvReaderService.readInBatches(context.resource(), context.mapper(),
                context.csvReadBatchSize())) {

            while (batches.hasNext()) {
                List<T> batch = batches.next();

                loomService.write(batch, context.persistFunction(), context.recordIdExtractor(), writeContext);

                batchExecutionService.saveFailedRecords(executionId, context.processor(), writeContext);
                batchExecutionService.checkpoint(executionId, writeContext);

                // Start a fresh context for the next chunk
                writeContext = new WriteContext();
            }

            batchExecutionService.markCompleted(executionId);
        } catch (Exception ex) {
            batchExecutionService.markFailed(executionId, writeContext, ex);
        }
    }
}
