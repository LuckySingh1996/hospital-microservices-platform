package com.hospital.platform.appointment.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	APPOINTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Appointment not found"),
	APPOINTMENT_CONFLICT(HttpStatus.CONFLICT, "Appointment time conflict"),
	INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "Invalid status transition"),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation error"),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
	KAFKA_PUBLISH_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "Failed to publish event to Kafka"),
	KAFKA_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "Kafka publish timeout"),
	KAFKA_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "Kafka operation interrupted"),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

	private final HttpStatus httpStatus;
	private final String defaultMessage;

	ErrorCode(HttpStatus httpStatus, String defaultMessage) {
		this.httpStatus = httpStatus;
		this.defaultMessage = defaultMessage;
	}

	public HttpStatus getHttpStatus() { return this.httpStatus; }

	public String getDefaultMessage() { return this.defaultMessage; }
}
