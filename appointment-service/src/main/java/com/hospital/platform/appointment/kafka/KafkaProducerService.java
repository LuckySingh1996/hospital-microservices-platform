package com.hospital.platform.appointment.kafka;

import com.hospital.platform.appointment.dto.AppointmentBookedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    
    private static final String APPOINTMENT_BOOKED_TOPIC = "appointment.booked";
    
    private final KafkaTemplate<String, AppointmentBookedEvent> kafkaTemplate;
    
    public void sendAppointmentBookedEvent(AppointmentBookedEvent event) {
        log.info("Sending appointment booked event for appointment: {}", 
                 event.getAppointmentNumber());
        
        CompletableFuture<SendResult<String, AppointmentBookedEvent>> future = 
            kafkaTemplate.send(APPOINTMENT_BOOKED_TOPIC, 
                             event.getAppointmentNumber(), 
                             event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent appointment booked event: {} to partition: {} with offset: {}",
                         event.getAppointmentNumber(),
                         result.getRecordMetadata().partition(),
                         result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send appointment booked event: {}", 
                         event.getAppointmentNumber(), ex);
                // In production, implement retry logic or dead letter queue
            }
        });
    }
}