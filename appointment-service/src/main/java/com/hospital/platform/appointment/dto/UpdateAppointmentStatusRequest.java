package com.hospital.platform.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentStatusRequest {
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "BOOKED|CHECKED_IN|CANCELLED|COMPLETED", 
             message = "Status must be one of: BOOKED, CHECKED_IN, CANCELLED, COMPLETED")
    private String status;
    
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}