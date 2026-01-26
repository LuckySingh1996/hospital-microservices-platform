package com.hospital.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillGeneratedEvent {
	private String eventId;
	private String billNumber;
	private Long billId;
	private Long appointmentId;
	private Long patientId;
	private BigDecimal totalAmount;
	private LocalDateTime eventTimestamp;
}