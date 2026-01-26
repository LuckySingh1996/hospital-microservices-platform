package com.hospital.platform.appointment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import com.hospital.platform.appointment.dto.AppointmentBookedEvent;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

	@Bean
	KafkaTemplate<String, AppointmentBookedEvent> kafkaTemplate(
			ProducerFactory<String, AppointmentBookedEvent> producerFactory) {
		return new KafkaTemplate<>(producerFactory);
	}

	@Bean
	RetryTemplate kafkaRetryTemplate() {
		RetryTemplate retryTemplate = new RetryTemplate();

		SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3); // 3 retries
		FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
		backOffPolicy.setBackOffPeriod(5000); // 5 sec backoff

		retryTemplate.setRetryPolicy(retryPolicy);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		return retryTemplate;
	}
}
