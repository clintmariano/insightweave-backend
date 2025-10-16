package com.insightweave.config;

import org.springframework.http.*;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Small helper to keep the envelope consistent
    private static ResponseEntity<Map<String, Object>> body(HttpStatus status, String error, String message, Map<String, ?> extra) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", status.value());
        map.put("error", error);
        if (message != null) map.put("message", message);
        if (extra != null && !extra.isEmpty()) map.putAll(extra);
        return ResponseEntity.status(status).body(map);
    }

    // 400: @Valid field errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> Map.of("field", err.getField(), "message", err.getDefaultMessage()))
                .toList();
        return body(HttpStatus.BAD_REQUEST, "Validation failed", null, Map.of("errors", errors));
    }

    // 404: repository delete on missing id
    @ExceptionHandler(org.springframework.dao.EmptyResultDataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDeleteMissing(org.springframework.dao.EmptyResultDataAccessException ex) {
        return body(HttpStatus.NOT_FOUND, "Not Found", "Resource not found", null);
    }

    // 405: wrong HTTP method
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> methodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return body(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", ex.getMessage(), null);
    }

    // 400: bad JSON / unreadable body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> unreadable(HttpMessageNotReadableException ex) {
        return body(HttpStatus.BAD_REQUEST, "Malformed JSON", "Request body is invalid JSON or has wrong types", null);
    }

    // 400: wrong param types (/api/docs/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> typeMismatch(MethodArgumentTypeMismatchException ex) {
        String msg = "Parameter '%s' expects type %s".formatted(
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        return body(HttpStatus.BAD_REQUEST, "Type Mismatch", msg, null);
    }

    // 400: missing required query param
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> missingParam(MissingServletRequestParameterException ex) {
        String msg = "Missing required parameter '%s'".formatted(ex.getParameterName());
        return body(HttpStatus.BAD_REQUEST, "Missing Parameter", msg, null);
    }

    // 4xx/5xx: anything thrown as ResponseStatusException (e.g., 404)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> responseStatus(ResponseStatusException ex) {
        String reason = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();

        HttpStatusCode code = ex.getStatusCode();
        String phrase = (code instanceof HttpStatus status)
                ? status.getReasonPhrase()
                : code.toString();

        return body(HttpStatus.valueOf(code.value()), phrase, reason, null);
    }

    // 400: generic bad request shortcut used in code
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException ex) {
        return body(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), null);
    }

    // 404: optional, if you sometimes throw NoSuchElementException
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        return body(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), null);
    }

    // 500: fallback (don’t leak internals)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", null);
    }
}
