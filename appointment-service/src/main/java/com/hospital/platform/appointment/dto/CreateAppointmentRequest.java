package com.hospital.platform.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Request DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {
    
    @NotNull(message = "Patient ID is required")
    @Positive(message = "Patient ID must be positive")
    private Long patientId;
    
    @NotBlank(message = "Patient name is required")
    @Size(max = 100, message = "Patient name must not exceed 100 characters")
    private String patientName;
    
    @NotNull(message = "Doctor ID is required")
    @Positive(message = "Doctor ID must be positive")
    private Long doctorId;
    
    @NotBlank(message = "Doctor name is required")
    @Size(max = 100, message = "Doctor name must not exceed 100 characters")
    private String doctorName;
    
    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
    
    @NotNull(message = "Appointment time is required")
    @Future(message = "Appointment time must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime appointmentTime;
    
    @Positive(message = "Duration must be positive")
    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 180, message = "Duration must not exceed 180 minutes")
    private Integer durationMinutes = 30;
    
    @NotNull(message = "Consultation fee is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Consultation fee must be positive")
    @Digits(integer = 8, fraction = 2, message = "Invalid consultation fee format")
    private BigDecimal consultationFee;
    
    @Size(max = 500, message = "Reason for visit must not exceed 500 characters")
    private String reasonForVisit;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}