package com.erebelo.springloomdemo.exception.model;

import java.time.Instant;
import org.springframework.http.HttpStatus;

public record ExceptionResponse(HttpStatus status, String message, Instant timestamp) {
}
