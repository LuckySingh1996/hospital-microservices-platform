package com.hospital.billing.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.hospital.billing.dto.BillGeneratedEvent;
import com.hospital.billing.dto.PaymentCompletedEvent;
import com.hospital.billing.dto.PaymentFailedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

	private static final String BILL_GENERATED_TOPIC = "bill.generated";
	private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
	private static final String PAYMENT_FAILED_TOPIC = "payment.failed";

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void sendBillGeneratedEvent(BillGeneratedEvent event) {
		log.info("Sending bill generated event: {}", event.getBillNumber());

		this.kafkaTemplate.send(BILL_GENERATED_TOPIC, event.getBillNumber(), event)
				.whenComplete((result, ex) -> {
					if (ex == null) {
						log.info("Bill generated event sent successfully: {}", event.getBillNumber());
					} else {
						log.error("Failed to send bill generated event: {}", event.getBillNumber(), ex);
					}
				});
	}

	public void sendPaymentCompletedEvent(PaymentCompletedEvent event) {
		log.info("Sending payment completed event: {}", event.getPaymentReference());

		this.kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, event.getPaymentReference(), event)
				.whenComplete((result, ex) -> {
					if (ex == null) {
						log.info("Payment completed event sent successfully: {}", event.getPaymentReference());
					} else {
						log.error("Failed to send payment completed event: {}", event.getPaymentReference(), ex);
					}
				});
	}

	public void sendPaymentFailedEvent(PaymentFailedEvent event) {
		log.info("Sending payment failed event for bill: {}", event.getBillId());

		this.kafkaTemplate.send(PAYMENT_FAILED_TOPIC, event.getEventId(), event)
				.whenComplete((result, ex) -> {
					if (ex == null) {
						log.info("Payment failed event sent successfully");
					} else {
						log.error("Failed to send payment failed event", ex);
					}
				});
	}
}