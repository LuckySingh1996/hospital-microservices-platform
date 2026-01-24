package com.hospital.platform.patient.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<ApiErrorResponse> handleApplicationException(ApplicationException ex,
			HttpServletRequest request) {

		ErrorCode errorCode = ex.getErrorCode();

		ApiErrorResponse response = new ApiErrorResponse(errorCode.getHttpStatus().value(),
				errorCode.getHttpStatus().getReasonPhrase(), ex.getMessage(), request.getRequestURI());

		return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
	}
}
