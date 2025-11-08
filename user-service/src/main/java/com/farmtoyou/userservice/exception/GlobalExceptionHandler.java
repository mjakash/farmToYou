package com.farmtoyou.userservice.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 
	 * @param ex
	 * @param request
	 * @return This will catch our specific exception
	 */
	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<?> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", HttpStatus.CONFLICT.value());
		body.put("error", "Conflict");
		body.put("message", ex.getMessage());
//		body.put("path", request.getDescription(false).replace("uri=", ""));

		return new ResponseEntity<>(body, HttpStatus.CONFLICT);
	}

	/**
	 * Handles failed login attempts
	 * 
	 * @param ex
	 * @return 401 Unauthorized response
	 */
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException ex) {
		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", HttpStatus.UNAUTHORIZED.value());
		body.put("error", "Unauthorized");
		body.put("message", "Invalid email or password");

		return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * 
	 * @param ex
	 * @param request
	 * @return This will handle rest of our exceptions
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {

		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		body.put("error", "Internal Server Error");
		body.put("message", "An unexpected error occurred: " + ex.getMessage());
		body.put("path", request.getDescription(false).replace("uri=", ""));

		return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
