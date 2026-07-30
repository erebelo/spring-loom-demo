package com.erebelo.springloomdemo.controller;

import com.erebelo.springloomdemo.model.dto.request.BatchExecutionRequest;
import com.erebelo.springloomdemo.model.dto.response.BatchExecutionResponse;
import com.erebelo.springloomdemo.model.dto.response.BatchProcessorsResponse;
import com.erebelo.springloomdemo.service.BatchContext;
import com.erebelo.springloomdemo.service.BatchContextRegistry;
import com.erebelo.springloomdemo.service.BatchOrchestratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/batch-executions")
@RequiredArgsConstructor
public class BatchExecutionController {

    private final BatchContextRegistry registry;
    private final BatchOrchestratorService service;

    @GetMapping("/processors")
    public BatchProcessorsResponse processors() {
        log.info("Listing available batch processors.");

        return new BatchProcessorsResponse(registry.processors());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BatchExecutionResponse start(@Valid @RequestBody BatchExecutionRequest request) {
        BatchContext<?> context = registry.get(request.processor());

        log.info("Submitting batch. processor={}", context.processor());

        String executionId = service.process(context);

        log.info("Batch submitted. executionId={}, processor={}", executionId, context.processor());

        return new BatchExecutionResponse(executionId,
                "%s batch submitted successfully.".formatted(context.processor()));
    }

    @PostMapping("/{executionId}/cancel")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BatchExecutionResponse cancel(@PathVariable String executionId) {
        log.info("Cancelling batch execution. executionId={}", executionId);

        service.cancel(executionId);

        log.info("Batch cancellation requested. executionId={}", executionId);

        return new BatchExecutionResponse(executionId, "Batch cancellation requested successfully.");
    }
}
