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
public class PaymentCompletedEvent {
	private String eventId;
	private String paymentReference;
	private Long billId;
	private String billNumber;
	private BigDecimal amount;
	private String paymentMethod;
	private LocalDateTime eventTimestamp;
}