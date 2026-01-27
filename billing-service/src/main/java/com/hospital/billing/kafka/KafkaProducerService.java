package com.hospital.billing.kafka;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import com.hospital.billing.dto.BillGeneratedEvent;
import com.hospital.billing.dto.PaymentCompletedEvent;
import com.hospital.billing.dto.PaymentFailedEvent;
import com.hospital.billing.exception.ApplicationException;
import com.hospital.billing.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

	private static final String BILL_GENERATED_TOPIC = "bill.generated";
	private static final String BILL_GENERATED_DLT = "bill.generated.dlt";

	private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
	private static final String PAYMENT_COMPLETED_DLT = "payment.completed.dlt";

	private static final String PAYMENT_FAILED_TOPIC = "payment.failed";
	private static final String PAYMENT_FAILED_DLT = "payment.failed.dlt";

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final RetryTemplate retryTemplate;

	// ✅ BILL GENERATED EVENT WITH RETRY & DLT
	public void sendBillGeneratedEvent(BillGeneratedEvent event) {
		log.info("Publishing bill generated event: {}", event.getBillNumber());

		try {
			this.retryTemplate.execute(context -> {
				try {
					this.kafkaTemplate.send(BILL_GENERATED_TOPIC,
							event.getBillNumber(),
							event)
							.get(5, TimeUnit.SECONDS); // 5 second timeout

					log.info("Kafka publish success (attempt {}) for bill {}",
							context.getRetryCount() + 1,
							event.getBillNumber());
					return null;

				} catch (TimeoutException te) {
					throw new ApplicationException(ErrorCode.KAFKA_TIMEOUT,
							"Kafka publish timeout after 5s");
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new ApplicationException(ErrorCode.KAFKA_INTERRUPTED,
							"Kafka operation interrupted");
				}
			});

		} catch (ApplicationException ae) {
			log.error("Kafka publish failed after retries for bill {}",
					event.getBillNumber(), ae);
			sendBillToDLT(event, ae);
			throw ae;

		} catch (Exception ex) {
			log.error("Unexpected Kafka publish failure for bill {}",
					event.getBillNumber(), ex);
			sendBillToDLT(event, ex);
			throw new ApplicationException(ErrorCode.KAFKA_PUBLISH_FAILED,
					"Failed to publish bill event to Kafka");
		}
	}

	// ✅ PAYMENT COMPLETED EVENT WITH RETRY & DLT
	public void sendPaymentCompletedEvent(PaymentCompletedEvent event) {
		log.info("Publishing payment completed event: {}", event.getPaymentReference());

		try {
			this.retryTemplate.execute(context -> {
				try {
					this.kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC,
							event.getPaymentReference(),
							event)
							.get(5, TimeUnit.SECONDS);

					log.info("Kafka publish success (attempt {}) for payment {}",
							context.getRetryCount() + 1,
							event.getPaymentReference());
					return null;

				} catch (TimeoutException te) {
					throw new ApplicationException(ErrorCode.KAFKA_TIMEOUT,
							"Kafka publish timeout after 5s");
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new ApplicationException(ErrorCode.KAFKA_INTERRUPTED,
							"Kafka operation interrupted");
				}
			});

		} catch (ApplicationException ae) {
			log.error("Kafka publish failed after retries for payment {}",
					event.getPaymentReference(), ae);
			sendPaymentCompletedToDLT(event, ae);
			throw ae;

		} catch (Exception ex) {
			log.error("Unexpected Kafka publish failure for payment {}",
					event.getPaymentReference(), ex);
			sendPaymentCompletedToDLT(event, ex);
			throw new ApplicationException(ErrorCode.KAFKA_PUBLISH_FAILED,
					"Failed to publish payment completed event to Kafka");
		}
	}

	// ✅ PAYMENT FAILED EVENT WITH RETRY & DLT
	public void sendPaymentFailedEvent(PaymentFailedEvent event) {
		log.info("Publishing payment failed event for bill: {}", event.getBillId());

		try {
			this.retryTemplate.execute(context -> {
				try {
					this.kafkaTemplate.send(PAYMENT_FAILED_TOPIC,
							event.getEventId(),
							event)
							.get(5, TimeUnit.SECONDS);

					log.info("Kafka publish success (attempt {}) for payment failed event",
							context.getRetryCount() + 1);
					return null;

				} catch (TimeoutException te) {
					throw new ApplicationException(ErrorCode.KAFKA_TIMEOUT,
							"Kafka publish timeout after 5s");
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new ApplicationException(ErrorCode.KAFKA_INTERRUPTED,
							"Kafka operation interrupted");
				}
			});

		} catch (ApplicationException ae) {
			log.error("Kafka publish failed after retries for payment failed event", ae);
			sendPaymentFailedToDLT(event, ae);
			throw ae;

		} catch (Exception ex) {
			log.error("Unexpected Kafka publish failure for payment failed event", ex);
			sendPaymentFailedToDLT(event, ex);
			throw new ApplicationException(ErrorCode.KAFKA_PUBLISH_FAILED,
					"Failed to publish payment failed event to Kafka");
		}
	}

	// ✅ DLT METHODS
	private void sendBillToDLT(BillGeneratedEvent event, Exception cause) {
		try {
			this.kafkaTemplate.send(BILL_GENERATED_DLT,
					event.getBillNumber(),
					event);
			log.error("Sent bill event to DLT: {} due to {}",
					event.getBillNumber(),
					cause.getMessage());
		} catch (Exception dltEx) {
			log.error("DLT publish ALSO FAILED for bill {}",
					event.getBillNumber(), dltEx);
		}
	}

	private void sendPaymentCompletedToDLT(PaymentCompletedEvent event, Exception cause) {
		try {
			this.kafkaTemplate.send(PAYMENT_COMPLETED_DLT,
					event.getPaymentReference(),
					event);
			log.error("Sent payment completed event to DLT: {} due to {}",
					event.getPaymentReference(),
					cause.getMessage());
		} catch (Exception dltEx) {
			log.error("DLT publish ALSO FAILED for payment {}",
					event.getPaymentReference(), dltEx);
		}
	}

	private void sendPaymentFailedToDLT(PaymentFailedEvent event, Exception cause) {
		try {
			this.kafkaTemplate.send(PAYMENT_FAILED_DLT,
					event.getEventId(),
					event);
			log.error("Sent payment failed event to DLT due to {}",
					cause.getMessage());
		} catch (Exception dltEx) {
			log.error("DLT publish ALSO FAILED for payment failed event", dltEx);
		}
	}
}