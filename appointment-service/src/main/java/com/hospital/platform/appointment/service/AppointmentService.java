package com.hospital.platform.appointment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.platform.appointment.dto.AppointmentBookedEvent;
import com.hospital.platform.appointment.dto.AppointmentResponse;
import com.hospital.platform.appointment.dto.CreateAppointmentRequest;
import com.hospital.platform.appointment.entity.Appointment;
import com.hospital.platform.appointment.entity.Appointment.AppointmentStatus;
import com.hospital.platform.appointment.exception.AppointmentConflictException;
import com.hospital.platform.appointment.exception.ResourceNotFoundException;
import com.hospital.platform.appointment.kafka.KafkaProducerService;
import com.hospital.platform.appointment.repository.AppointmentRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

	private final AppointmentRepository appointmentRepository;
	private final KafkaProducerService kafkaProducerService;
	private final MeterRegistry meterRegistry;

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public AppointmentResponse createAppointment(CreateAppointmentRequest request, String username) {
		log.info("Creating appointment for patient: {} with doctor: {}", request.getPatientId(), request.getDoctorId());

		validateAppointmentTime(request.getAppointmentTime());

		// Check for overlapping appointments with pessimistic locking
		LocalDateTime endTime = request.getAppointmentTime()
				.plusMinutes(request.getDurationMinutes());

		List<Appointment> overlapping = this.appointmentRepository.findOverlappingAppointments(
				request.getDoctorId(),
				request.getAppointmentTime(),
				endTime);

		if (!overlapping.isEmpty()) {
			log.warn("Overlapping appointment found for doctor: {} at time: {}",
					request.getDoctorId(), request.getAppointmentTime());
			throw new AppointmentConflictException(
					"Doctor is not available at the requested time. Overlapping appointment exists.");
		}

		// Create appointment entity
		Appointment appointment = Appointment.builder()
				.appointmentNumber(generateAppointmentNumber())
				.patientId(request.getPatientId())
				.patientName(request.getPatientName())
				.doctorId(request.getDoctorId())
				.doctorName(request.getDoctorName())
				.department(request.getDepartment())
				.appointmentTime(request.getAppointmentTime())
				.durationMinutes(request.getDurationMinutes())
				.consultationFee(request.getConsultationFee())
				.reasonForVisit(request.getReasonForVisit())
				.notes(request.getNotes())
				.status(AppointmentStatus.BOOKED)
				.createdBy(username)
				.isDeleted(false)
				.build();

		// Save appointment
		Appointment savedAppointment = this.appointmentRepository.save(appointment);
		log.info("Appointment created successfully: {}", savedAppointment.getAppointmentNumber());

		// Increment metrics
		Counter.builder("appointments_booked_total")
				.tag("department", request.getDepartment())
				.register(this.meterRegistry)
				.increment();

		// Publish Kafka event
		publishAppointmentBookedEvent(savedAppointment);

		return mapToResponse(savedAppointment);
	}

	@Transactional(readOnly = true)
	public AppointmentResponse getAppointment(Long appointmentId) {
		log.info("Fetching appointment with ID: {}", appointmentId);

		Appointment appointment = this.appointmentRepository.findById(appointmentId)
				.filter(a -> !a.getIsDeleted())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Appointment not found with ID: " + appointmentId));

		return mapToResponse(appointment);
	}

	private void validateAppointmentTime(LocalDateTime appointmentTime) {
		if (appointmentTime.isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Appointment time cannot be in the past");
		}

		int hour = appointmentTime.getHour();
		if (hour < 8 || hour >= 18) {
			throw new IllegalArgumentException(
					"Appointments can only be booked between 8:00 AM and 6:00 PM");
		}
	}

	private String generateAppointmentNumber() {
		return "APT-" + LocalDateTime.now().getYear() + "-" +
				UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

	private void publishAppointmentBookedEvent(Appointment appointment) {
		AppointmentBookedEvent event = AppointmentBookedEvent.builder()
				.eventId(UUID.randomUUID().toString())
				.appointmentNumber(appointment.getAppointmentNumber())
				.appointmentId(appointment.getId())
				.patientId(appointment.getPatientId())
				.patientName(appointment.getPatientName())
				.doctorId(appointment.getDoctorId())
				.doctorName(appointment.getDoctorName())
				.department(appointment.getDepartment())
				.appointmentTime(appointment.getAppointmentTime())
				.consultationFee(appointment.getConsultationFee())
				.reasonForVisit(appointment.getReasonForVisit())
				.eventTimestamp(LocalDateTime.now())
				.createdBy(appointment.getCreatedBy())
				.build();

		this.kafkaProducerService.sendAppointmentBookedEvent(event);
	}

	private AppointmentResponse mapToResponse(Appointment appointment) {
		return AppointmentResponse.builder()
				.id(appointment.getId())
				.appointmentNumber(appointment.getAppointmentNumber())
				.patientId(appointment.getPatientId())
				.patientName(appointment.getPatientName())
				.doctorId(appointment.getDoctorId())
				.doctorName(appointment.getDoctorName())
				.department(appointment.getDepartment())
				.appointmentTime(appointment.getAppointmentTime())
				.durationMinutes(appointment.getDurationMinutes())
				.status(appointment.getStatus().name())
				.consultationFee(appointment.getConsultationFee())
				.reasonForVisit(appointment.getReasonForVisit())
				.notes(appointment.getNotes())
				.cancellationReason(appointment.getCancellationReason())
				.cancelledAt(appointment.getCancelledAt())
				.checkedInAt(appointment.getCheckedInAt())
				.completedAt(appointment.getCompletedAt())
				.createdAt(appointment.getCreatedAt())
				.updatedAt(appointment.getUpdatedAt())
				.build();
	}
}