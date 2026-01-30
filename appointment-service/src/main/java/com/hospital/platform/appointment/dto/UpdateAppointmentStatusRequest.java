package com.hospital.platform.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentStatusRequest {

	@NotBlank(message = "Status is required")
	@Pattern(regexp = "BOOKED|CHECKED_IN|CANCELLED|COMPLETED", message = "Status must be one of: BOOKED, CHECKED_IN, CANCELLED, COMPLETED")
	private String status;

	@Size(max = 500, message = "Reason must not exceed 500 characters")
	private String reason;
}