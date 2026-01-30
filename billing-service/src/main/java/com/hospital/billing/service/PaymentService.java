package com.hospital.billing.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.billing.dao.BillDao;
import com.hospital.billing.dao.PaymentDao;
import com.hospital.billing.dto.CreatePaymentRequest;
import com.hospital.billing.dto.PaymentCompletedEvent;
import com.hospital.billing.dto.PaymentFailedEvent;
import com.hospital.billing.dto.PaymentResponse;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.entity.Payment;
import com.hospital.billing.exception.ApplicationException;
import com.hospital.billing.exception.ErrorCode;
import com.hospital.billing.kafka.KafkaProducerService;
import com.hospital.billing.mapper.PaymentMapper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

	private final PaymentDao paymentDao;
	private final BillDao billingDao;
	private final BillingService billingService;
	private final KafkaProducerService kafkaProducerService;
	private final MeterRegistry meterRegistry;
	private final PaymentMapper mapper;

	@Transactional
	public PaymentResponse processPayment(CreatePaymentRequest request) {
		log.info("Processing payment for bill: {}", request.getBillId());

		var bill = validatePaymentRequest(request);
		Payment payment = this.mapper.fromrequest(request);

		try {
			String transactionId = processWithPaymentGateway(request);
			payment.setTransactionId(transactionId);
			payment.setStatus(Payment.PaymentStatus.COMPLETED);

			this.billingService.updateBillPayment(request.getBillId(), request.getAmount());

			Payment savedPayment = this.paymentDao.save(payment);

			log.info("Payment completed: {}", savedPayment.getPaymentReference());

			// Increment success metric
			Counter.builder("payments_success_total")
					.register(this.meterRegistry)
					.increment();

			publishPaymentCompletedEvent(savedPayment, bill.getBillNumber());

			return mapToResponse(savedPayment);

		} catch (Exception e) {
			log.error("Payment processing failed", e);

			payment.setStatus(Payment.PaymentStatus.FAILED);
			payment.setFailureReason(e.getMessage());
			this.paymentDao.save(payment);

			// Increment failure metric
			Counter.builder("payments_failure_total")
					.register(this.meterRegistry)
					.increment();

			publishPaymentFailedEvent(request.getBillId(), request.getAmount(), e.getMessage());

			throw new ApplicationException(ErrorCode.PAYMENT_FAILED, "Payment processing failed: " + e.getMessage());
		}
	}

	private Bill validatePaymentRequest(CreatePaymentRequest request) {

		// Idempotency check - prevent double payment
		var existingPayment = this.paymentDao.existsByIdempotencyKey(request.getIdempotencyKey());
		if (existingPayment) {
			log.warn("Duplicate payment attempt detected: {}", request.getIdempotencyKey());
			throw new ApplicationException(ErrorCode.DUPLICATE_PAYMENT, "Payment already processed with this idempotency key");
		}

		// Verify bill exists
		var bill = this.billingDao.findByIdForUpdate(request.getBillId())
				.orElseThrow(() -> new ApplicationException(ErrorCode.BILL_NOT_FOUND, "Bill not found: " + request.getBillId()));

		// Validate payment amount
		if (request.getAmount().compareTo(bill.getDueAmount()) > 0) {
			throw new ApplicationException(ErrorCode.PAYMENT_FAILED, "Payment amount exceeds due amount");
		}
		return bill;
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

	private void publishPaymentCompletedEvent(Payment payment, String billNumber) {
		PaymentCompletedEvent event = this.mapper.toPaymentCompletedEvent(payment, billNumber);
		this.kafkaProducerService.sendPaymentCompletedEvent(event);
	}

	private void publishPaymentFailedEvent(Long billId, BigDecimal amount, String reason) {
		PaymentFailedEvent event = this.mapper.toPaymentFailedEvent(billId, amount, reason);
		this.kafkaProducerService.sendPaymentFailedEvent(event);
	}

	private PaymentResponse mapToResponse(Payment payment) {
		return this.mapper.toResponse(payment);
	}

}