package com.erebelo.springloomdemo.exception;

import com.erebelo.springloomdemo.exception.model.BadRequestException;
import com.erebelo.springloomdemo.exception.model.ConflictException;
import com.erebelo.springloomdemo.exception.model.NotFoundException;
import com.erebelo.springloomdemo.exception.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleBadRequestException(BadRequestException exception) {
        log.error("Request failed.", exception);
        return createResponse(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse handleNotFoundException(NotFoundException exception) {
        log.error("Resource not found.", exception);
        return createResponse(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleConstraintViolationException(ConstraintViolationException exception) {
        log.error("Validation failed.", exception);

        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage()).sorted()
                .collect(Collectors.joining(", "));

        return createResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse handleConflictException(ConflictException exception) {
        log.error("Request conflict.", exception);
        return createResponse(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResponse handleUnexpectedException(Exception exception) {
        log.error("Unexpected error occurred.", exception);
        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    private ErrorResponse createResponse(HttpStatus status, Exception exception) {
        return createResponse(status, exception.getMessage());
    }

    private ErrorResponse createResponse(HttpStatus status, String message) {
        if (message == null || message.isBlank()) {
            message = "No message available.";
        }

        return new ErrorResponse(status, message, Instant.now());
    }
}
