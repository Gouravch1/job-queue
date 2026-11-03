
package com.gourav.jobqueue_backend.exception;

import com.gourav.jobqueue_backend.processing.exception.JobProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.springframework.validation.FieldError;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {

        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                validationErrors
        );

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            JobNotFoundException ex,
            HttpServletRequest request
    ) {

        return buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(DuplicateIdempotencyKeyException.class)
    public ResponseEntity<ApiError> handleDuplicateKey(
            DuplicateIdempotencyKeyException ex,
            HttpServletRequest request
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDatabaseConflict(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {

        return buildError(
                HttpStatus.CONFLICT,
                "Request conflicts with existing database data.",
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError :
                ex.getBindingResult().getFieldErrors()) {

            errors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Request validation failed.",
                request.getRequestURI(),
                errors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleInvalidJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Invalid JSON request body or data format.",
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Invalid value for parameter: " + ex.getName(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error(
                "Unexpected error while processing {}",
                request.getRequestURI(),
                ex
        );

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal server error occurred.",
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(JobProcessingException.class)
    public ResponseEntity<ApiError> handleJobProcessingException(
            JobProcessingException ex,
            HttpServletRequest request
    ) {

        HttpStatus status = ex.isRetryable()
                ? HttpStatus.SERVICE_UNAVAILABLE
                : HttpStatus.UNPROCESSABLE_ENTITY;

        return buildError(
                status,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }
}
