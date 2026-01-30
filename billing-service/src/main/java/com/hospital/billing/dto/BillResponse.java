package com.hospital.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {

	private Long id;
	private String billNumber;
	private Long appointmentId;
	private Long patientId;
	private String patientName;
	private BigDecimal consultationFee;
	private BigDecimal labCharges;
	private BigDecimal pharmacyCharges;
	private BigDecimal totalAmount;
	private BigDecimal paidAmount;
	private BigDecimal dueAmount;
	private String status;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime updatedAt;
}