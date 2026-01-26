package com.hospital.billing.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Create Payment Request
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

	@NotNull(message = "Bill ID is required")
	private Long billId;

	@NotNull(message = "Amount is required")
	@DecimalMin(value = "0.01", message = "Amount must be greater than zero")
	private BigDecimal amount;

	@NotBlank(message = "Payment method is required")
	@Pattern(regexp = "CASH|CARD|UPI|NET_BANKING", message = "Invalid payment method")
	private String paymentMethod;

	@NotBlank(message = "Idempotency key is required")
	private String idempotencyKey;
}