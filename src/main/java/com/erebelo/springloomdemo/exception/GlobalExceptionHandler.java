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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - Client errors

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleBadRequestException(BadRequestException exception) {
        log.warn("Bad request.", exception);

        return createResponse(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.warn("Malformed request body.");

        return createResponse(HttpStatus.BAD_REQUEST, "Invalid request body.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        log.warn("Request body validation failed.");

        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage()).sorted()
                .collect(Collectors.joining(", "));

        return createResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleConstraintViolationException(ConstraintViolationException exception) {
        log.warn("Constraint validation failed.");

        String message = exception.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage()).sorted().collect(Collectors.joining(", "));

        return createResponse(HttpStatus.BAD_REQUEST, message);
    }

    // 404 - Not found

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse handleNotFoundException(NotFoundException exception) {
        log.warn("Resource not found.", exception);

        return createResponse(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse handleNoResourceFoundException(NoResourceFoundException exception) {
        log.warn("Resource not found. path={}", exception.getResourcePath());

        return createResponse(HttpStatus.NOT_FOUND, "Endpoint not found.");
    }

    // 409 - Conflict

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse handleConflictException(ConflictException exception) {
        log.warn("Request conflict.", exception);

        return createResponse(HttpStatus.CONFLICT, exception);
    }

    // 500 - Server errors

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResponse handleUnexpectedException(Exception exception) {
        log.error("Unexpected error.", exception);

        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
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
