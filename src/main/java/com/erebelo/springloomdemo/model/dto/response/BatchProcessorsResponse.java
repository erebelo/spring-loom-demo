package com.erebelo.springloomdemo.model.dto.response;

import com.erebelo.springloomdemo.model.enums.BatchProcessorName;
import java.util.Set;

public record BatchProcessorsResponse(Set<BatchProcessorName> processors) {
}
