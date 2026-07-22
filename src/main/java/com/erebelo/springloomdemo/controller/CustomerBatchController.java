package com.erebelo.springloomdemo.controller;

import com.erebelo.springloomdemo.model.dto.response.BatchResponse;
import com.erebelo.springloomdemo.service.BatchOrchestratorService;
import com.erebelo.springloomdemo.service.customer.CustomerBatchContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerBatchController {

    private final BatchOrchestratorService service;
    private final CustomerBatchContext context;

    @PostMapping(value = "/batch/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<@NonNull BatchResponse> triggerCustomerHydration() {
        log.info("Batch hydration request received. processor={}, method=POST, endpoint=/customers/batch/start",
                context.processor());

        String executionId = service.process(context);

        return ResponseEntity.accepted().body(new BatchResponse(executionId, "Customer batch submitted successfully"));
    }
}
