package com.erebelo.springloomdemo.service.batch;

import com.erebelo.springloomdemo.exception.model.NotFoundException;
import com.erebelo.springloomdemo.model.dto.WriteContext;
import com.erebelo.springloomdemo.service.batch.execution.BatchExecutionService;
import com.erebelo.springloomdemo.service.batch.execution.ExecutionRegistry;
import com.erebelo.springloomdemo.service.batch.processor.BatchProcessor;
import com.erebelo.springloomdemo.service.csv.CsvBatchReader;
import com.erebelo.springloomdemo.service.csv.CsvReaderService;
import com.erebelo.springloomdemo.service.loom.LoomService;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BatchOrchestratorService {

    private final ExecutorService batchExecutor;
    private final ExecutionRegistry executionRegistry;
    private final BatchExecutionService batchExecutionService;
    private final CsvReaderService csvReaderService;
    private final LoomService loomService;

    public BatchOrchestratorService(@Qualifier("batchExecutor") ExecutorService batchExecutor,
            ExecutionRegistry executionRegistry, BatchExecutionService batchExecutionService,
            CsvReaderService csvReaderService, LoomService loomService) {
        this.batchExecutor = batchExecutor;
        this.executionRegistry = executionRegistry;
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
    public <T> String process(BatchProcessor<T> processor) {
        String executionId = "bulk-exec-" + UUID.randomUUID().toString().substring(0, 18);
        log.info("Starting batch execution. executionId={}, processor={}", executionId, processor.processorName());

        batchExecutionService.startExecution(executionId, processor.processorName(), processor.staleTimeout(),
                executionRegistry::containsExecution);

        Future<Void> future = batchExecutor.submit(() -> {
            executeBatch(executionId, processor);
            return null;
        });

        executionRegistry.register(executionId, future);

        return executionId;
    }

    /**
     * Executes the batch asynchronously by processing the CSV in chunks.
     * <p>
     * A checkpoint is persisted after each chunk so progress, successful records,
     * and failed records are preserved even if the batch terminates unexpectedly.
     */
    private <T> void executeBatch(String executionId, BatchProcessor<T> processor) {
        long startTime = System.nanoTime();
        WriteContext writeContext = new WriteContext();
        int checkpointNumber = 0;

        log.info("Batch execution started. executionId={}, processor={}, batchSize={}", executionId,
                processor.processorName(), processor.csvReadBatchSize());

        try (CsvBatchReader<T> batches = csvReaderService.readInBatches(processor.resource(), processor.mapper(),
                processor.csvReadBatchSize())) {

            while (batches.hasNext()) {
                List<T> batch = batches.next();

                loomService.write(batch, processor.persistFunction(), processor.recordIdExtractor(), writeContext);

                batchExecutionService.saveFailedRecords(executionId, processor.processorName(), writeContext);
                batchExecutionService.checkpoint(executionId, writeContext);
                checkpointNumber++;

                log.debug(
                        "Batch checkpoint completed. executionId={}, processor={}, checkpointNumber={}, recordsProcessed={}, successes={}, failures={}",
                        executionId, processor.processorName(), checkpointNumber, batch.size(),
                        writeContext.getSuccessCount().get(), writeContext.getErrors().size());

                // Start a fresh context for the next chunk
                writeContext = new WriteContext();
            }

            batchExecutionService.markCompleted(executionId);

            log.info("Batch execution completed. executionId={}, processor={}, checkpoints={}, duration={}",
                    executionId, processor.processorName(), checkpointNumber, formatDuration(startTime));
        } catch (InterruptedException ex) {
            boolean interrupted = Thread.interrupted();

            try {
                batchExecutionService.saveFailedRecords(executionId, processor.processorName(), writeContext);

                if (executionRegistry.isCancellationRequested(executionId)) {
                    batchExecutionService.markCancelled(executionId, writeContext);
                } else {
                    batchExecutionService.markFailed(executionId, writeContext, ex);
                }

                log.error(
                        "Batch execution interrupted. executionId={}, processor={}, checkpoints={}, duration={}, successes={}, failures={}",
                        executionId, processor.processorName(), checkpointNumber, formatDuration(startTime),
                        writeContext.getSuccessCount().get(), writeContext.getErrors().size(), ex);
            } finally {
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception ex) {
            batchExecutionService.saveFailedRecords(executionId, processor.processorName(), writeContext);
            batchExecutionService.markFailed(executionId, writeContext, ex);

            log.error(
                    "Batch execution failed. executionId={}, processor={}, checkpoints={}, duration={}, successes={}, failures={}",
                    executionId, processor.processorName(), checkpointNumber, formatDuration(startTime),
                    writeContext.getSuccessCount().get(), writeContext.getErrors().size(), ex);
        } finally {
            executionRegistry.remove(executionId);
        }
    }

    /**
     * Requests cancellation of a running batch execution.
     * <p>
     * Cancellation is cooperative. Interrupting the coordinator Virtual Thread
     * prevents additional work from being scheduled while allowing any in-flight
     * persistence operations to complete safely.
     */
    public void cancel(String executionId) {
        Future<Void> future = executionRegistry.get(executionId);

        if (future == null) {
            throw new NotFoundException(
                    "No active batch execution managed by this application was found for executionId: " + executionId
                            + ". If the execution is still marked as RUNNING, it will be automatically recovered when a new execution for the same "
                            + "processor is started.");
        }

        executionRegistry.requestCancellation(executionId);

        future.cancel(true);

        log.info("Batch cancellation requested. executionId={}", executionId);
    }

    private static String formatDuration(long startTime) {
        long totalSeconds = Duration.ofNanos(System.nanoTime() - startTime).toSeconds();
        return String.format("%dm%02ds", totalSeconds / 60, totalSeconds % 60);
    }
}
