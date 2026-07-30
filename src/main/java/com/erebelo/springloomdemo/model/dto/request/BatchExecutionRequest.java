package com.erebelo.springloomdemo.model.dto.request;

import com.erebelo.springloomdemo.model.enums.BatchProcessor;
import jakarta.validation.constraints.NotNull;

public record BatchExecutionRequest(@NotNull(message = "is mandatory") BatchProcessor processor) {
}
