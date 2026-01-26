package com.hospital.billing.exception;

public class PaymentProcessingException extends RuntimeException {
	public PaymentProcessingException(String message) {
		super(message);
	}
}

// GlobalExceptionHandler.java
