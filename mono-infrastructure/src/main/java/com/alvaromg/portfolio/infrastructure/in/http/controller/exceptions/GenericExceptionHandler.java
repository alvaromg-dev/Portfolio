package com.alvaromg.portfolio.infrastructure.in.http.controller.exceptions;

import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.alvaromg.portfolio.application.shared.exceptions.AppRuntimeException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GenericExceptionHandler {

    @ExceptionHandler(AppRuntimeException.class)
    public ResponseEntity<ProblemDetail> handleAppRuntimeException(AppRuntimeException ex) {
        HttpStatus status = httpStatusFromErrorCode(ex.getErrorCode());
        log.warn("AppRuntimeException handled with status {}: {}", status.value(), ex.getMessage());
        return ResponseEntity.status(status).body(buildDetail(status, ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(status).body(buildDetail(status, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(status).body(buildDetail(status, "Internal Server Error"));
    }

    private HttpStatus httpStatusFromErrorCode(int errorCode) {
        for (HttpStatus status : HttpStatus.values()) {
            if (status.value() == errorCode) {
                return status;
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ProblemDetail buildDetail(HttpStatus status, String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setDetail(message);
        problemDetail.setTitle("Error " + status.value());
        problemDetail.setProperty("timestamp", OffsetDateTime.now());
        return problemDetail;
    }
}
