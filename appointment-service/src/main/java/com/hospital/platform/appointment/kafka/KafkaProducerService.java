package com.hospital.platform.appointment.kafka;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import com.hospital.platform.appointment.dto.AppointmentBookedEvent;
import com.hospital.platform.appointment.exception.ApplicationException;
import com.hospital.platform.appointment.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

	private static final String APPOINTMENT_BOOKED_TOPIC = "appointment.booked";
	private static final String APPOINTMENT_BOOKED_DLT = "appointment.booked.dlt";

	private final KafkaTemplate<String, AppointmentBookedEvent> kafkaTemplate;
	private final RetryTemplate retryTemplate;

	public void sendAppointmentBookedEvent(AppointmentBookedEvent event) {
		log.info("Publishing appointment booked event: {}", event.getAppointmentNumber());

		try {
			this.retryTemplate.execute(context -> {
				try {
					this.kafkaTemplate.send(APPOINTMENT_BOOKED_TOPIC,
							event.getAppointmentNumber(), event)
							.get(5, TimeUnit.SECONDS); // 5s timeout

					log.info("Kafka publish success (attempt {}) for {}", context.getRetryCount() + 1, event.getAppointmentNumber());
					return null;
				} catch (TimeoutException te) {
					throw new ApplicationException(ErrorCode.KAFKA_TIMEOUT, "Kafka publish timeout after 5s");
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new ApplicationException(ErrorCode.KAFKA_INTERRUPTED, "Kafka operation interrupted");
				}
			});

		} catch (ApplicationException ae) {
			log.error("Kafka publish failed after retries for {}", event.getAppointmentNumber(), ae);
			sendToDeadLetterTopic(event, ae);
			throw ae;

		} catch (Exception ex) {
			log.error("Unexpected Kafka publish failure for {}",
					event.getAppointmentNumber(), ex);
			sendToDeadLetterTopic(event, ex);
			throw new ApplicationException(ErrorCode.KAFKA_PUBLISH_FAILED,
					"Failed to publish event to Kafka");
		}
	}

	private void sendToDeadLetterTopic(AppointmentBookedEvent event, Exception cause) {
		try {
			this.kafkaTemplate.send(APPOINTMENT_BOOKED_DLT,
					event.getAppointmentNumber(),
					event);

			log.error("Sent event to DLT: {} due to {}",
					event.getAppointmentNumber(),
					cause.getMessage());

		} catch (Exception dltEx) {
			log.error("DLT publish ALSO FAILED for {}",
					event.getAppointmentNumber(), dltEx);
		}
	}
}
