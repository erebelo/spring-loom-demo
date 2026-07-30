package com.erebelo.springloomdemo.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

@Component
public class ExecutionRegistry {

    private final ConcurrentMap<String, Future<Void>> executions = new ConcurrentHashMap<>();
    private final Map<String, AtomicBoolean> cancellations = new ConcurrentHashMap<>();

    public void register(String executionId, Future<Void> future) {
        executions.put(executionId, future);
        cancellations.put(executionId, new AtomicBoolean(false));
    }

    public Future<Void> get(String executionId) {
        return executions.get(executionId);
    }

    public void requestCancellation(String executionId) {
        AtomicBoolean cancellation = cancellations.get(executionId);

        if (cancellation != null) {
            cancellation.set(true);
        }
    }

    public boolean isCancellationRequested(String executionId) {
        AtomicBoolean cancellation = cancellations.get(executionId);
        return cancellation != null && cancellation.get();
    }

    public void remove(String executionId) {
        executions.remove(executionId);
        cancellations.remove(executionId);
    }
}
