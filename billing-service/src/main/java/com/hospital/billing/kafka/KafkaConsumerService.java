package com.hospital.billing.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import com.hospital.billing.dto.AppointmentBookedEvent;
import com.hospital.billing.exception.ApplicationException;
import com.hospital.billing.exception.ErrorCode;
import com.hospital.billing.service.BillingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

	private final BillingService billingService;
	private final RetryTemplate retryTemplate;
	private final org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

	private static final String APPOINTMENT_BOOKED_DLT = "appointment.booked.dlt.billing";

	@KafkaListener(topics = "appointment.booked", groupId = "billing-service-group", containerFactory = "kafkaListenerContainerFactory")
	public void consumeAppointmentBookedEvent(
			@Payload AppointmentBookedEvent event,
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
			@Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
			@Header(KafkaHeaders.OFFSET) long offset,
			Acknowledgment acknowledgment) {

		log.info("Received appointment booked event from topic: {}, partition: {}, offset: {}",
				topic, partition, offset);

		try {
			this.retryTemplate.execute(context -> {
				log.info("Processing appointment booked event (attempt {}): {}",
						context.getRetryCount() + 1,
						event.getAppointmentNumber());

				this.billingService.handleAppointmentBooked(event);
				return null;
			});

			acknowledgment.acknowledge();
			log.info("Successfully processed appointment booked event: {}",
					event.getAppointmentNumber());

		} catch (ApplicationException ex) {
			if (ex.getErrorCode() == ErrorCode.DUPLICATE_BILL) {
				log.warn("Duplicate bill for appointment: {} - Acknowledging and skipping",
						event.getAppointmentId());
				acknowledgment.acknowledge();
				return;
			}

			log.error("Business failure processing appointment {} after retries - Sending to DLT",
					event.getAppointmentNumber(), ex);
			sendConsumerToDLT(event, ex);
			acknowledgment.acknowledge();

		} catch (Exception ex) {
			log.error("Technical failure processing appointment {} after retries - Sending to DLT",
					event.getAppointmentNumber(), ex);
			sendConsumerToDLT(event, ex);
			acknowledgment.acknowledge();
		}
	}

	private void sendConsumerToDLT(AppointmentBookedEvent event, Exception cause) {
		try {
			this.kafkaTemplate.send(APPOINTMENT_BOOKED_DLT,
					event.getAppointmentNumber(),
					event);
			log.error("Sent consumer event to DLT: {} due to {}",
					event.getAppointmentNumber(),
					cause.getMessage());
		} catch (Exception dltEx) {
			log.error("DLT publish ALSO FAILED for consumer event {}",
					event.getAppointmentNumber(), dltEx);
		}
	}
}