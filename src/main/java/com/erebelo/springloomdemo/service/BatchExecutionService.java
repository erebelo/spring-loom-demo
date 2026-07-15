package com.erebelo.springloomdemo.service;

import com.erebelo.springloomdemo.domain.dto.WriteResultDto;
import org.springframework.stereotype.Service;

@Service
public class BatchExecutionService {

    public void create(String executionId) {
    }

    public void markRunning(String executionId) {
    }

    public void markCompleted(String executionId, WriteResultDto writeResult) {
    }

    public void markFailed(String executionId, Exception ex) {
    }
}
