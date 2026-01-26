package com.hospital.platform.appointment.exception;

public class ApplicationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final ErrorCode errorCode;
	private final String details;

	public ApplicationException(ErrorCode errorCode) {
		super(errorCode.getDefaultMessage());
		this.errorCode = errorCode;
		this.details = null;
	}

	public ApplicationException(ErrorCode errorCode, String details) {
		super(details != null ? details : errorCode.getDefaultMessage());
		this.errorCode = errorCode;
		this.details = details;
	}

	public ApplicationException(ErrorCode errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
		this.details = "";
	}

	public ErrorCode getErrorCode() { return this.errorCode; }

	public String getDetails() { return this.details; }
}
