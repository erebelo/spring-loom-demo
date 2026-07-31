package com.erebelo.springloomdemo.service.batch.processor;

import com.erebelo.springloomdemo.exception.model.BadRequestException;
import com.erebelo.springloomdemo.model.enums.BatchProcessorName;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class BatchProcessorRegistry {

    private final Map<BatchProcessorName, BatchProcessor<?>> processors;

    public BatchProcessorRegistry(List<BatchProcessor<?>> processors) {
        this.processors = processors.stream()
                .collect(Collectors.toMap(BatchProcessor::processorName, Function.identity()));
    }

    @SuppressWarnings("java:S1452")
    public BatchProcessor<?> get(BatchProcessorName processorName) {
        BatchProcessor<?> processor = processors.get(processorName);

        if (processor == null) {
            throw new BadRequestException("Unsupported batch processor: " + processorName);
        }

        return processor;
    }

    public Set<BatchProcessorName> processors() {
        return processors.keySet();
    }
}
