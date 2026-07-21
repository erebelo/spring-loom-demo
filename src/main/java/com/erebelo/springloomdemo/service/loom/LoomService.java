package com.erebelo.springloomdemo.service.loom;

import com.erebelo.springloomdemo.domain.model.WriteContext;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Loom is simply the codename of the Java project that introduced virtual
 * threads.
 */
@Service
public class LoomService {

    private final ExecutorService workerExecutor;

    /*
     * Maximum number of tasks submitted to the executor at once.
     *
     * Although Virtual Threads are lightweight, each submitted task also creates a
     * Future and other JVM objects. Processing records in chunks keeps memory usage
     * predictable while still allowing high concurrency.
     */
    private static final int CHUNK_SIZE = 1_000;

    /*
     * Limits the number of concurrent database operations.
     *
     * Virtual Threads are inexpensive to create, but the database connection pool
     * is limited. The semaphore provides back-pressure by allowing only a fixed
     * number of tasks to execute the persistence logic simultaneously.
     */
    private final Semaphore semaphore;

    public LoomService(@Qualifier("workerExecutor") ExecutorService workerExecutor,
            @Value("${batch.loom.semaphore.max-permits:25}") int permits) {
        this.workerExecutor = workerExecutor;
        this.semaphore = new Semaphore(permits);
    }

    public <T> void write(List<T> batch, Consumer<T> persistFunction, Function<T, String> recordIdExtractor,
            WriteContext writeContext) throws InterruptedException {
        /*
         * Process records in chunks to avoid creating millions of Future and Virtual
         * Thread objects simultaneously.
         */
        for (int i = 0; i < batch.size(); i += CHUNK_SIZE) {
            List<T> chunk = batch.subList(i, Math.min(i + CHUNK_SIZE, batch.size()));

            /*
             * One task = One Virtual Thread (Java 21 recommended model). The JVM schedules
             * these lightweight threads onto a small pool of carrier (OS) threads.
             */
            List<Future<Void>> futures = chunk.stream().map(item -> workerExecutor.submit((Callable<Void>) () -> {
                String recordId = recordIdExtractor.apply(item);
                boolean acquired = false;

                try {
                    semaphore.acquire();
                    acquired = true;

                    persistFunction.accept(item);

                    writeContext.incrementSuccess();
                    return null;
                } catch (Exception ex) {
                    if (ex instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }

                    writeContext.addError(new WriteContext.ItemError(recordId, ex, item));
                    return null;
                } finally {
                    if (acquired) {
                        semaphore.release();
                    }
                }
            })).toList();

            /*
             * Wait for every task in the current chunk to finish.
             *
             * Future.get() blocks only this coordinating thread while the corresponding
             * Virtual Thread completes.
             *
             * If a Virtual Thread is blocked waiting on I/O or the semaphore, the JVM
             * releases its carrier thread to execute other work.
             *
             * Waiting for one chunk at a time keeps the number of active Future objects
             * bounded for very large batches.
             */
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw ex;
                } catch (ExecutionException ex) {
                    throw new IllegalStateException("Unexpected error while waiting for batch tasks to complete.",
                            ex.getCause());
                }
            }
        }
    }
}
