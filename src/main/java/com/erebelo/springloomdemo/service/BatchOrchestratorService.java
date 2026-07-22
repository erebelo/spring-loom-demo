package com.erebelo.springloomdemo.service;

import com.erebelo.springloomdemo.model.context.WriteContext;
import com.erebelo.springloomdemo.service.csv.CsvBatchReader;
import com.erebelo.springloomdemo.service.csv.CsvReaderService;
import com.erebelo.springloomdemo.service.loom.LoomService;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
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

        log.info("Starting batch execution. executionId={}, processor={}", executionId, context.processor());

        batchExecutionService.createExecutionIfAvailable(executionId, context.processor());
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
        long startTime = System.nanoTime();

        batchExecutionService.markRunning(executionId);

        WriteContext writeContext = new WriteContext();
        int checkpointNumber = 0;

        log.info("Batch execution started. executionId={}, processor={}, batchSize={}", executionId,
                context.processor(), context.csvReadBatchSize());

        try (CsvBatchReader<T> batches = csvReaderService.readInBatches(context.resource(), context.mapper(),
                context.csvReadBatchSize())) {

            while (batches.hasNext()) {
                List<T> batch = batches.next();

                loomService.write(batch, context.persistFunction(), context.recordIdExtractor(), writeContext);

                batchExecutionService.saveFailedRecords(executionId, context.processor(), writeContext);

                batchExecutionService.checkpoint(executionId, writeContext);
                checkpointNumber++;

                log.debug(
                        "Batch checkpoint completed. executionId={}, processor={}, checkpointNumber={}, recordsProcessed={}, successes={}, "
                                + "failures={}",
                        executionId, context.processor(), checkpointNumber, batch.size(),
                        writeContext.getSuccessCount().get(), writeContext.getErrors().size());

                // Start a fresh context for the next chunk
                writeContext = new WriteContext();
            }

            batchExecutionService.markCompleted(executionId);

            log.info("Batch execution completed. executionId={}, processor={}, checkpoints={}, duration={}",
                    executionId, context.processor(), checkpointNumber, formatDuration(startTime));
        } catch (Exception ex) {
            batchExecutionService.markFailed(executionId, writeContext, ex);

            log.error(
                    "Batch execution failed. executionId={}, processor={}, checkpoints={}, duration={}, successes={}, failures={}",
                    executionId, context.processor(), checkpointNumber, formatDuration(startTime),
                    writeContext.getSuccessCount().get(), writeContext.getErrors().size(), ex);
        }
    }

    private static String formatDuration(long startTime) {
        long totalSeconds = Duration.ofNanos(System.nanoTime() - startTime).toSeconds();
        return String.format("%dm%02ds", totalSeconds / 60, totalSeconds % 60);
    }
}
