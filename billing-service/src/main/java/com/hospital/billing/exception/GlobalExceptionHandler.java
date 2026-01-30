package com.hospital.billing.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.KafkaException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<ErrorResponse> handleApplicationException(
			ApplicationException ex,
			HttpServletRequest request) {

		ErrorCode code = ex.getErrorCode();

		ErrorResponse error = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(code.getHttpStatus().value())
				.error(code.name())
				.message(ex.getMessage())
				.build();

		log.error("ApplicationException [{}]: {}", code.name(), ex.getMessage());

		return ResponseEntity
				.status(code.getHttpStatus())
				.body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(
			MethodArgumentNotValidException ex) {

		log.error("Validation failed", ex);

		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		ErrorResponse error = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(ErrorCode.VALIDATION_ERROR.getHttpStatus().value())
				.error(ErrorCode.VALIDATION_ERROR.name())
				.message("Invalid input parameters")
				.validationErrors(errors)
				.build();

		return ResponseEntity
				.status(ErrorCode.VALIDATION_ERROR.getHttpStatus())
				.body(error);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(
			AccessDeniedException ex) {

		log.error("Access denied: {}", ex.getMessage());

		ErrorResponse error = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(ErrorCode.UNAUTHORIZED.getHttpStatus().value())
				.error(ErrorCode.UNAUTHORIZED.name())
				.message("Access denied")
				.build();

		return ResponseEntity
				.status(ErrorCode.UNAUTHORIZED.getHttpStatus())
				.body(error);
	}

	@ExceptionHandler(KafkaException.class)
	public ResponseEntity<ErrorResponse> handleKafkaException(
			Exception ex,
			HttpServletRequest request) {

		log.error("Kafka exception", ex);

		ErrorResponse error = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(ErrorCode.INTERNAL_ERROR.getHttpStatus().value())
				.error(ErrorCode.INTERNAL_ERROR.name())
				.message(ex.getMessage())
				.build();

		return ResponseEntity
				.status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
				.body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(
			Exception ex,
			HttpServletRequest request) {

		log.error("Unhandled exception", ex);

		ErrorResponse error = ErrorResponse.builder()
				.timestamp(LocalDateTime.now())
				.status(ErrorCode.INTERNAL_ERROR.getHttpStatus().value())
				.error(ErrorCode.INTERNAL_ERROR.name())
				.message("An unexpected error occurred")
				.build();

		return ResponseEntity
				.status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
				.body(error);
	}

}
