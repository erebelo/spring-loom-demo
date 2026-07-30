package com.erebelo.springloomdemo.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import org.springframework.stereotype.Component;

@Component
public class ExecutionRegistry {

    private final ConcurrentMap<String, Future<Void>> executions = new ConcurrentHashMap<>();

    public void register(String executionId, Future<Void> future) {
        executions.put(executionId, future);
    }

    public Future<Void> get(String executionId) {
        return executions.get(executionId);
    }

    public void remove(String executionId) {
        executions.remove(executionId);
    }
}
