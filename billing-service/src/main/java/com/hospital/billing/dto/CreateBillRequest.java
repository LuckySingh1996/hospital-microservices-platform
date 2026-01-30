package com.hospital.billing.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBillRequest {

	@NotNull(message = "Appointment ID is required")
	private Long appointmentId;

	@NotNull(message = "Patient ID is required")
	private Long patientId;

	@NotBlank(message = "Patient name is required")
	private String patientName;

	@NotNull(message = "Consultation fee is required")
	@DecimalMin(value = "0.0", message = "Consultation fee must be positive")
	private BigDecimal consultationFee;

	@DecimalMin(value = "0.0", message = "Lab charges must be positive")
	private BigDecimal labCharges;

	@DecimalMin(value = "0.0", message = "Pharmacy charges must be positive")
	private BigDecimal pharmacyCharges;
}