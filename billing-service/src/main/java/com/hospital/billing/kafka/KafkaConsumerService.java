package com.hospital.billing.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.hospital.billing.dto.AppointmentBookedEvent;
import com.hospital.billing.service.BillingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

	private final BillingService billingService;

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
			this.billingService.handleAppointmentBooked(event);
			acknowledgment.acknowledge();
			log.info("Successfully processed appointment booked event: {}", event.getAppointmentNumber());

		} catch (Exception e) {
			log.error("Failed to process appointment booked event: {}", event.getAppointmentNumber(), e);
			// Don't acknowledge - message will be redelivered
			throw e;
		}
	}
}