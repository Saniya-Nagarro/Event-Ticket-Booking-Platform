package com.event.ticketing.booking_service.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.event.ticketing.booking_service.metrics.BookingMetrics;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private final BookingMetrics bookingMetrics;

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
		return build(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
				.map(error -> error.getField() + " " + error.getDefaultMessage()).orElse("Validation failed");
		return build(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler({ HttpClientErrorException.class, HttpServerErrorException.class })
	public ResponseEntity<Map<String, Object>> handleServiceCall(Exception ex) {
		return build(HttpStatus.BAD_REQUEST, "Event Service call failed: " + ex.getMessage());
	}

	private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
		bookingMetrics.incrementError();

		Map<String, Object> response = new HashMap<>();
		response.put("timestamp", LocalDateTime.now());
		response.put("status", status.value());
		response.put("message", message);
		return ResponseEntity.status(status).body(response);
	}
}
