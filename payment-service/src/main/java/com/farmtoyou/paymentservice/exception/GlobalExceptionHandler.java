package com.farmtoyou.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // Makes it a global handler
public class GlobalExceptionHandler {

    /**
     * Catches 415 Unsupported Media Type.
     */
    @ExceptionHandler(UnsupportedMediaTypeStatusException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedMediaType(UnsupportedMediaTypeStatusException ex) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        body.put("error", "Unsupported Media Type");
        body.put("message", "Media type is not supported. Please use application/json.");

        return new ResponseEntity<>(body, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Catches 405 Method Not Allowed.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.METHOD_NOT_ALLOWED.value());
        body.put("error", "Method Not Allowed");
        body.put("message", "This endpoint does not support the " + ex.getMethod() + " method.");

        return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * This method catches any other unexpected exceptions.
     * It returns a 500 Internal Server Error, also without the path.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred."); // Don't leak internal details

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}