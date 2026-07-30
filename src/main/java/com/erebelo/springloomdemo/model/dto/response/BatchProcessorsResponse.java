package com.erebelo.springloomdemo.model.dto.response;

import com.erebelo.springloomdemo.model.enums.BatchProcessor;
import java.util.Set;

public record BatchProcessorsResponse(Set<BatchProcessor> processors) {
}
