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
public class PaymentFailedEvent {
	private String eventId;
	private Long billId;
	private BigDecimal amount;
	private String reason;
	private LocalDateTime eventTimestamp;
}