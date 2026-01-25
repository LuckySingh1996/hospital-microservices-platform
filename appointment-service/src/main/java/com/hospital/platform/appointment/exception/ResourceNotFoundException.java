package com.hospital.platform.appointment.exception;

// Resource Not Found Exception
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}