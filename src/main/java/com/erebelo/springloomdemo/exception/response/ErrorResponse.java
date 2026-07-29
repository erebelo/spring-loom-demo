package com.erebelo.springloomdemo.exception.response;

import java.time.Instant;
import org.springframework.http.HttpStatus;

public record ErrorResponse(HttpStatus status, String message, Instant timestamp) {
}
