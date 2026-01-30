package com.hospital.platform.appointment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.platform.appointment.dto.AppointmentBookedEvent;
import com.hospital.platform.appointment.dto.AppointmentResponse;
import com.hospital.platform.appointment.dto.CreateAppointmentRequest;
import com.hospital.platform.appointment.entity.AppointmentEntity;
import com.hospital.platform.appointment.exception.ApplicationException;
import com.hospital.platform.appointment.exception.ErrorCode;
import com.hospital.platform.appointment.kafka.KafkaProducerService;
import com.hospital.platform.appointment.mapper.AppointmentMapper;
import com.hospital.platform.appointment.repository.AppointmentDao;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

	private final AppointmentDao dao;
	private final KafkaProducerService kafkaProducerService;
	private final MeterRegistry meterRegistry;
	private final AppointmentMapper mapper;

	@Transactional
	public AppointmentResponse createAppointment(CreateAppointmentRequest request, String username) {
		log.info("Creating appointment for patient: {} with doctor: {}", request.getPatientId(), request.getDoctorId());

		validateAppointmentTime(request.getAppointmentTime());

		LocalDateTime endTime = request.getAppointmentTime()
				.plusMinutes(request.getDurationMinutes());

		List<AppointmentEntity> overlapping = this.dao.findOverlappingAppointments(
				request.getDoctorId(),
				request.getAppointmentTime(),
				endTime);

		if (!overlapping.isEmpty()) {
			log.warn("Overlapping appointment found for doctor: {} at time: {}",
					request.getDoctorId(), request.getAppointmentTime());
			throw new ApplicationException(ErrorCode.APPOINTMENT_CONFLICT,
					"Doctor is not available at the requested time. Overlapping appointment exists.");
		}

		AppointmentEntity savedAppointment = this.dao.save(this.mapper.fromRequest(request, username));
		log.info("Appointment created successfully: {}", savedAppointment.getAppointmentNumber());

		Counter.builder("appointments_booked_total")
				.tag("department", request.getDepartment())
				.register(this.meterRegistry)
				.increment();

		publishAppointmentBookedEvent(savedAppointment);

		return this.mapper.toResponse(savedAppointment);
	}

	public AppointmentResponse getAppointment(Long appointmentId) {
		log.info("Fetching appointment with ID: {}", appointmentId);

		AppointmentEntity appointment = this.dao.findByAppointmentId(appointmentId).orElseThrow(() -> new ApplicationException(ErrorCode.APPOINTMENT_NOT_FOUND,
				"Appointment not found with ID: " + appointmentId));

		return this.mapper.toResponse(appointment);
	}

	private void validateAppointmentTime(LocalDateTime appointmentTime) {
		if (appointmentTime.isBefore(LocalDateTime.now())) {
			throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "Appointment time cannot be in the past");
		}

		int hour = appointmentTime.getHour();
		if (hour < 8 || hour >= 18) {
			throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "Appointments can only be booked between 8:00 AM and 6:00 PM");
		}
	}

	private void publishAppointmentBookedEvent(AppointmentEntity appointment) {
		AppointmentBookedEvent event = this.mapper.fromAppointmentEntity(appointment);

		this.kafkaProducerService.sendAppointmentBookedEvent(event);
	}

}