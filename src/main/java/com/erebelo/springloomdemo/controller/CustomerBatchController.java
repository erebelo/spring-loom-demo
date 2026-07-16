package com.erebelo.springloomdemo.controller;

import static com.erebelo.springloomdemo.constant.BusinessConstant.CUSTOMERS_BATCH_PATH;
import static com.erebelo.springloomdemo.constant.BusinessConstant.CUSTOMERS_PATH;

import com.erebelo.springloomdemo.domain.dto.BatchResponseDto;
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
@RequestMapping(CUSTOMERS_PATH)
@RequiredArgsConstructor
public class CustomerBatchController {

    private final BatchOrchestratorService service;
    private final CustomerBatchContext context;

    @PostMapping(value = CUSTOMERS_BATCH_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<@NonNull BatchResponseDto> triggerCustomerHydration() {
        log.info("POST {}", CUSTOMERS_PATH + CUSTOMERS_BATCH_PATH);
        String executionId = service.process(context);
        return ResponseEntity.accepted()
                .body(new BatchResponseDto(executionId, "Customer batch submitted successfully"));
    }
}
