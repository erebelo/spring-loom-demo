package com.erebelo.springloomdemo.service.loom;

import com.erebelo.springloomdemo.domain.dto.WriteResultDto;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/*
 * Loom is simply the codename of the Java project that introduced virtual threads.
 */

@Service
public class LoomService {

    private final ExecutorService workerExecutor;

    /*
     * Number of tasks submitted concurrently.
     *
     * Virtual threads are lightweight, but each submitted task also creates a
     * Future and other JVM objects. Chunking keeps memory usage bounded while still
     * allowing the JVM to efficiently schedule virtual threads.
     */
    private static final int CHUNK_SIZE = 25_000;

    /*
     * Limits the number of concurrent database operations.
     *
     * Virtual threads are inexpensive to create, but databases still have a limited
     * number of available connections. The semaphore provides back-pressure by
     * allowing only N tasks to execute the persistence logic simultaneously.
     */
    private final Semaphore semaphore = new Semaphore(50);

    public LoomService(@Qualifier("workerExecutor") ExecutorService workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    public <T> WriteResultDto write(List<T> batch, Consumer<T> persistFunction, Function<T, String> recordIdExtractor) {
        List<WriteResultDto.ItemErrorDto> errors = new CopyOnWriteArrayList<>();
        long successCount = 0;

        /*
         * Process the input in chunks to avoid creating millions of
         * Future/VirtualThread objects simultaneously.
         */
        for (int i = 0; i < batch.size(); i += CHUNK_SIZE) {
            List<T> chunk = batch.subList(i, Math.min(i + CHUNK_SIZE, batch.size()));

            /*
             * One task = One Virtual Thread (Java 21 recommended model). The JVM schedules
             * these lightweight threads onto a small pool of carrier (OS) threads.
             */
            List<Future<Boolean>> futures = chunk.stream().map(item -> workerExecutor.submit(() -> {
                String recordId = recordIdExtractor.apply(item);
                boolean acquired = false;

                try {
                    semaphore.acquire();
                    acquired = true;

                    persistFunction.accept(item);
                    return true;
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }

                    errors.add(new WriteResultDto.ItemErrorDto(recordId, e, item));
                    return false;
                } finally {
                    if (acquired) {
                        semaphore.release();
                    }
                }
            })).toList();

            /*
             * Wait for every task in the current chunk to finish. Future.get() blocks only
             * this coordinating thread until the corresponding virtual thread completes.
             *
             * While virtual threads are waiting (DB, I/O, semaphore, etc.), the JVM
             * releases their carrier threads to execute other work.
             *
             * Processing one chunk at a time keeps the number of active
             * Future/VirtualThread objects bounded, avoiding excessive memory usage for
             * very large batches (e.g. millions of records).
             */

            successCount += futures.stream().map(future -> {
                try {
                    return future.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                } catch (ExecutionException e) {
                    return false;
                }
            }).filter(Boolean::booleanValue).count();
        }

        return errors.isEmpty()
                ? WriteResultDto.success(successCount)
                : WriteResultDto.withErrors(successCount, errors);
    }
}
