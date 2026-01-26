package com.hospital.billing.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.billing.dao.BillRepository;
import com.hospital.billing.dao.PaymentRepository;
import com.hospital.billing.dto.CreatePaymentRequest;
import com.hospital.billing.dto.PaymentCompletedEvent;
import com.hospital.billing.dto.PaymentFailedEvent;
import com.hospital.billing.dto.PaymentResponse;
import com.hospital.billing.entity.Payment;
import com.hospital.billing.exception.DuplicatePaymentException;
import com.hospital.billing.exception.PaymentProcessingException;
import com.hospital.billing.kafka.KafkaProducerService;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final BillRepository billRepository;
	private final BillingService billingService;
	private final KafkaProducerService kafkaProducerService;
	private final MeterRegistry meterRegistry;

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public PaymentResponse processPayment(CreatePaymentRequest request) {
		log.info("Processing payment for bill: {}", request.getBillId());

		// Idempotency check - prevent double payment
		var existingPayment = this.paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
		if (existingPayment.isPresent()) {
			log.warn("Duplicate payment attempt detected: {}", request.getIdempotencyKey());
			throw new DuplicatePaymentException("Payment already processed with this idempotency key");
		}

		// Verify bill exists
		var bill = this.billRepository.findById(request.getBillId())
				.orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + request.getBillId()));

		// Validate payment amount
		if (request.getAmount().compareTo(bill.getDueAmount()) > 0) {
			throw new PaymentProcessingException("Payment amount exceeds due amount");
		}

		Payment payment = Payment.builder()
				.paymentReference(generatePaymentReference())
				.billId(request.getBillId())
				.amount(request.getAmount())
				.paymentMethod(Payment.PaymentMethod.valueOf(request.getPaymentMethod()))
				.idempotencyKey(request.getIdempotencyKey())
				.status(Payment.PaymentStatus.PENDING)
				.build();

		try {
			// Simulate payment gateway processing
			String transactionId = processWithPaymentGateway(request);
			payment.setTransactionId(transactionId);
			payment.setStatus(Payment.PaymentStatus.COMPLETED);

			// Save payment
			Payment savedPayment = this.paymentRepository.save(payment);

			// Update bill
			this.billingService.updateBillPayment(request.getBillId(), request.getAmount());

			log.info("Payment completed: {}", savedPayment.getPaymentReference());

			// Increment success metric
			Counter.builder("payments_success_total")
					.register(this.meterRegistry)
					.increment();

			// Publish success event
			publishPaymentCompletedEvent(savedPayment, bill.getBillNumber());

			return mapToResponse(savedPayment);

		} catch (Exception e) {
			log.error("Payment processing failed", e);

			payment.setStatus(Payment.PaymentStatus.FAILED);
			payment.setFailureReason(e.getMessage());
			this.paymentRepository.save(payment);

			// Increment failure metric
			Counter.builder("payments_failure_total")
					.register(this.meterRegistry)
					.increment();

			// Publish failure event
			publishPaymentFailedEvent(request.getBillId(), request.getAmount(), e.getMessage());

			throw new PaymentProcessingException("Payment processing failed: " + e.getMessage());
		}
	}

	@Transactional(readOnly = true)
	public PaymentResponse getPayment(String paymentReference) {
		log.info("Fetching payment: {}", paymentReference);

		Payment payment = this.paymentRepository.findByPaymentReference(paymentReference)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentReference));

		return mapToResponse(payment);
	}

	private String processWithPaymentGateway(CreatePaymentRequest request) {
		// Simulate payment gateway call
		// In real scenario, this would call actual payment gateway API
		log.info("Processing payment with gateway...");

		// Simulate processing time
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// Generate transaction ID
		return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
	}

	private String generatePaymentReference() {
		return "PAY-" + LocalDateTime.now().getYear() + "-" +
				UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

	private void publishPaymentCompletedEvent(Payment payment, String billNumber) {
		PaymentCompletedEvent event = PaymentCompletedEvent.builder()
				.eventId(UUID.randomUUID().toString())
				.paymentReference(payment.getPaymentReference())
				.billId(payment.getBillId())
				.billNumber(billNumber)
				.amount(payment.getAmount())
				.paymentMethod(payment.getPaymentMethod().name())
				.eventTimestamp(LocalDateTime.now())
				.build();

		this.kafkaProducerService.sendPaymentCompletedEvent(event);
	}

	private void publishPaymentFailedEvent(Long billId, BigDecimal amount, String reason) {
		PaymentFailedEvent event = PaymentFailedEvent.builder()
				.eventId(UUID.randomUUID().toString())
				.billId(billId)
				.amount(amount)
				.reason(reason)
				.eventTimestamp(LocalDateTime.now())
				.build();

		this.kafkaProducerService.sendPaymentFailedEvent(event);
	}

	private PaymentResponse mapToResponse(Payment payment) {
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
}