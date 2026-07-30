package com.erebelo.springloomdemo.service;

import com.erebelo.springloomdemo.exception.model.BadRequestException;
import com.erebelo.springloomdemo.model.enums.BatchProcessor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class BatchContextRegistry {

    private final Map<BatchProcessor, BatchContext<?>> contexts;

    public BatchContextRegistry(List<BatchContext<?>> contexts) {
        this.contexts = contexts.stream().collect(Collectors.toMap(BatchContext::processor, Function.identity()));
    }

    @SuppressWarnings("java:S1452")
    public BatchContext<?> get(BatchProcessor processor) {
        BatchContext<?> context = contexts.get(processor);

        if (context == null) {
            throw new BadRequestException("Unsupported batch processor: " + processor);
        }

        return context;
    }

    public Set<BatchProcessor> processors() {
        return contexts.keySet();
    }
}
