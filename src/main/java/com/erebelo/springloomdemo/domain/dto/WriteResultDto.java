package com.erebelo.springloomdemo.domain.dto;

import java.util.List;

public record WriteResultDto(long successCount, List<ItemErrorDto> errors) {

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public static WriteResultDto success(long successCount) {
        return new WriteResultDto(successCount, List.of());
    }

    public static WriteResultDto withErrors(long successCount, List<ItemErrorDto> errors) {
        return new WriteResultDto(successCount, List.copyOf(errors));
    }

    public record ItemErrorDto(String recordId, Throwable exception, Object item) {
    }
}
