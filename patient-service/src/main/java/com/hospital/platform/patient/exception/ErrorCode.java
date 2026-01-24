package com.hospital.platform.patient.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	INVALID_MOBILE(HttpStatus.BAD_REQUEST, "Invalid mobile number"),
	INVALID_NAME(HttpStatus.BAD_REQUEST, "Invalid name input"),
	INVALID_EMAIL(HttpStatus.BAD_REQUEST, "Invalid email address"),
	INVALID_DOB(HttpStatus.BAD_REQUEST, "Invalid date of birth"),
	PATIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Patient not found"),
	PATIENT_ALREADY_DELETED(HttpStatus.CONFLICT, "Patient already deleted"),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

	private final HttpStatus httpStatus;
	private final String defaultMessage;

	ErrorCode(HttpStatus httpStatus, String defaultMessage) {
		this.httpStatus = httpStatus;
		this.defaultMessage = defaultMessage;
	}

	public HttpStatus getHttpStatus() { return httpStatus; }

	public String getDefaultMessage() { return defaultMessage; }
}
