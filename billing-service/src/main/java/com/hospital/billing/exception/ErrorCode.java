package com.hospital.billing.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	BILL_NOT_FOUND(HttpStatus.NOT_FOUND, "Bill not found"),
	DUPLICATE_BILL(HttpStatus.CONFLICT, "Duplicate bill"),
	DUPLICATE_PAYMENT(HttpStatus.CONFLICT, "Duplicate payment"),
	PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "Payment processing failed"),
	PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Payment not found"),
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
