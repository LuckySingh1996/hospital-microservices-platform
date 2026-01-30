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
public class AppointmentBookedEvent {
	private String eventId;
	private String appointmentNumber;
	private Long appointmentId;
	private Long patientId;
	private String patientName;
	private BigDecimal consultationFee;
	private LocalDateTime eventTimestamp;
}