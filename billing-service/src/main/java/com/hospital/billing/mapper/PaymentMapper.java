package com.hospital.billing.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.hospital.billing.dto.CreatePaymentRequest;
import com.hospital.billing.dto.PaymentCompletedEvent;
import com.hospital.billing.dto.PaymentFailedEvent;
import com.hospital.billing.dto.PaymentResponse;
import com.hospital.billing.entity.Payment;

@Component
public class PaymentMapper {

	public PaymentResponse toResponse(Payment payment) {
		return PaymentResponse.builder()
				.id(payment.getId())
				.paymentReference(payment.getPaymentReference())
				.billId(payment.getBillId())
				.amount(payment.getAmount())
				.paymentMethod(payment.getPaymentMethod().name())
				.status(payment.getStatus().name())
				.transactionId(payment.getTransactionId())
				.failureReason(payment.getFailureReason())
				.createdAt(payment.getCreatedAt())
				.build();
	}

	public PaymentFailedEvent toPaymentFailedEvent(Long billId, BigDecimal amount, String reason) {
		return PaymentFailedEvent.builder()
				.eventId(UUID.randomUUID().toString())
				.billId(billId)
				.amount(amount)
				.reason(reason)
				.eventTimestamp(LocalDateTime.now())
				.build();
	}

	public PaymentCompletedEvent toPaymentCompletedEvent(Payment payment, String billNumber) {
		return PaymentCompletedEvent.builder()
				.eventId(UUID.randomUUID().toString())
				.paymentReference(payment.getPaymentReference())
				.billId(payment.getBillId())
				.billNumber(billNumber)
				.amount(payment.getAmount())
				.paymentMethod(payment.getPaymentMethod().name())
				.eventTimestamp(LocalDateTime.now())
				.build();
	}

	public Payment fromrequest(CreatePaymentRequest request) {
		Payment payment = new Payment();

		payment.setPaymentReference(generatePaymentReference());
		payment.setBillId(request.getBillId());
		payment.setAmount(request.getAmount());
		payment.setPaymentMethod(Payment.PaymentMethod.valueOf(request.getPaymentMethod()));
		payment.setIdempotencyKey(request.getIdempotencyKey());
		payment.setStatus(Payment.PaymentStatus.PENDING);

		return payment;

	}

	private String generatePaymentReference() {
		return "PAY-" + LocalDateTime.now().getYear() + "-" +
				UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

}
