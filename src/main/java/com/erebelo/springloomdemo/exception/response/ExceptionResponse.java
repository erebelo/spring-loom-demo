package com.erebelo.springloomdemo.exception.response;

import java.time.Instant;
import org.springframework.http.HttpStatus;

public record ExceptionResponse(HttpStatus status, String message, Instant timestamp) {
}
