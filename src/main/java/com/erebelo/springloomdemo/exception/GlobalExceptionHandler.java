package com.erebelo.springloomdemo.exception;

import com.erebelo.springloomdemo.exception.model.BadRequestException;
import com.erebelo.springloomdemo.exception.model.ConflictException;
import com.erebelo.springloomdemo.exception.model.NotFoundException;
import com.erebelo.springloomdemo.exception.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.ObjectUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.InvalidTypeIdException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - Client errors

    /**
     * Handles application-level bad request errors.
     */
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleBadRequestException(BadRequestException exception) {
        log.warn("Bad request.", exception);

        return createResponse(HttpStatus.BAD_REQUEST, exception);
    }

    /**
     * Handles malformed JSON and invalid JSON values, including enum
     * deserialization.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.warn("Malformed request body.");

        String message = "Invalid request body.";

        if (exception.getCause() instanceof InvalidFormatException e && e.getTargetType().isEnum()) {
            String field = e.getPath().stream().map(JacksonException.Reference::getPropertyName)
                    .filter(Objects::nonNull).collect(Collectors.joining("."));

            message = "Invalid value '%s' for field '%s'. Allowed values: %s.".formatted(e.getValue(), field,
                    getEnumValues(e.getTargetType()));
        } else if (exception.getCause() instanceof InvalidTypeIdException e && e.getMessage() != null
                && !e.getMessage().isBlank()) {
            message = e.getMessage();
        }

        return createResponse(HttpStatus.BAD_REQUEST, message);
    }

    /** Handles missing required request parameters. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        log.warn("Missing request parameter.");

        String message = "Required parameter '%s' is missing.".formatted(exception.getParameterName());

        return createResponse(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Handles invalid path-variable and request-parameter type conversions.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        log.warn("Invalid request parameter.");

        String message = "Invalid value '%s' for parameter '%s'.".formatted(exception.getValue(), exception.getName());

        if (exception.getRequiredType() != null && exception.getRequiredType().isEnum()) {
            message = "Invalid value '%s' for parameter '%s'. Allowed values: %s.".formatted(exception.getValue(),
                    exception.getName(), getEnumValues(exception.getRequiredType()));
        }

        return createResponse(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Handles Bean Validation failures on @RequestBody objects.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        log.warn("Request body validation failed.");

        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> "'%s' %s".formatted(error.getField(), error.getDefaultMessage())).sorted()
                .collect(Collectors.joining(", ", "", "."));

        return createResponse(HttpStatus.BAD_REQUEST, message);
    }

    /** Handles Bean Validation constraint violations on method parameters. */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleConstraintViolationException(ConstraintViolationException exception) {
        log.warn("Constraint validation failed.");

        String message = exception.getConstraintViolations().stream().map(violation -> {
            String property = violation.getPropertyPath().toString();

            return "'%s' %s".formatted(property, violation.getMessage());
        }).sorted().collect(Collectors.joining(", ", "", "."));

        return createResponse(HttpStatus.BAD_REQUEST, message);
    }

    // 404 - Not found

    /** Handles application-level resource-not-found errors. */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse handleNotFoundException(NotFoundException exception) {
        log.warn("Resource not found.", exception);

        return createResponse(HttpStatus.NOT_FOUND, exception);
    }

    /** Handles requests to endpoints that do not exist. */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse handleNoResourceFoundException(NoResourceFoundException exception) {
        log.warn("Resource not found. path={}", exception.getResourcePath());

        return createResponse(HttpStatus.NOT_FOUND, "Endpoint not found.");
    }

    // 405 - Method not allowed

    /**
     * Handles requests using an HTTP method unsupported by the endpoint.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    ErrorResponse handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        log.warn("Method not allowed.", exception);

        String message = exception.getMessage();
        String[] supportedHttpMethods = exception.getSupportedMethods();

        if (!ObjectUtils.isEmpty(supportedHttpMethods)) {
            message += ". Supported methods: " + String.join(", ", supportedHttpMethods) + ".";
        }

        return createResponse(HttpStatus.METHOD_NOT_ALLOWED, message);
    }

    // 409 - Conflict

    /** Handles application-level resource or state conflicts. */
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse handleConflictException(ConflictException exception) {
        log.warn("Request conflict.", exception);

        return createResponse(HttpStatus.CONFLICT, exception);
    }

    // 500 - Server errors

    /** Handles unexpected server-side exceptions not handled explicitly above. */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResponse handleUnexpectedException(Exception exception) {
        log.error("Unexpected error.", exception);

        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    /**
     * Extracts the available values from an enum type.
     */
    private String getEnumValues(Class<?> enumType) {
        return Arrays.stream(enumType.getEnumConstants()).map(Object::toString).collect(Collectors.joining(", "));
    }

    /** Creates an error response using an exception message. */
    private ErrorResponse createResponse(HttpStatus status, Exception exception) {
        return createResponse(status, exception.getMessage());
    }

    /** Creates an error response with a fallback message when necessary. */
    private ErrorResponse createResponse(HttpStatus status, String message) {
        if (message == null || message.isBlank()) {
            message = "No message available.";
        }

        return new ErrorResponse(status, message, Instant.now());
    }
}
