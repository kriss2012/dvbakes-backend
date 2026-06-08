package com.dvbakes.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * Global exception handler for DvBakes API.
 * Converts Spring Security and business logic exceptions into consistent JSON responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 401 Unauthorized — missing or invalid JWT token.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Unauthorized",
                "message", ex.getMessage() != null ? ex.getMessage() : "Authentication required.",
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * 403 Forbidden — authenticated but insufficient role.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "Forbidden",
                "message", "You do not have permission to perform this action. Admin JWT required.",
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * 400 Bad Request — business logic errors (out of stock, empty cart, etc.)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", ex.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    /**
     * 404 Not Found — entity not found errors.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred.";
        // Distinguish 404 cases from generic 500s
        if (msg.contains("not found") || msg.contains("Not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", msg,
                    "timestamp", Instant.now().toString()
            ));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", msg,
                "timestamp", Instant.now().toString()
        ));
    }
}
