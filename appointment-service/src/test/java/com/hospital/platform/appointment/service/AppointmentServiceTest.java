package com.hospital.platform.appointment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hospital.platform.appointment.dto.AppointmentResponse;
import com.hospital.platform.appointment.dto.CreateAppointmentRequest;
import com.hospital.platform.appointment.entity.Appointment;
import com.hospital.platform.appointment.entity.Appointment.AppointmentStatus;
import com.hospital.platform.appointment.exception.AppointmentConflictException;
import com.hospital.platform.appointment.exception.ResourceNotFoundException;
import com.hospital.platform.appointment.kafka.KafkaProducerService;
import com.hospital.platform.appointment.repository.AppointmentRepository;
import com.hospital.platform.appointment.service.AppointmentService;

import io.micrometer.core.instrument.MeterRegistry;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

	@Mock
	private AppointmentRepository appointmentRepository;

	@Mock
	private KafkaProducerService kafkaProducerService;

	@Mock
	private MeterRegistry meterRegistry;

	@InjectMocks
	private AppointmentService appointmentService;

	private CreateAppointmentRequest request;
	private Appointment appointment;

	@BeforeEach
	void setUp() {
		this.request = CreateAppointmentRequest.builder()
				.patientId(1L)
				.patientName("John Doe")
				.doctorId(1L)
				.doctorName("Dr. Smith")
				.department("Cardiology")
				.appointmentTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
				.durationMinutes(30)
				.consultationFee(new BigDecimal("500.00"))
				.reasonForVisit("Regular checkup")
				.build();

		this.appointment = Appointment.builder()
				.id(1L)
				.appointmentNumber("APT-2025-ABC123")
				.patientId(this.request.getPatientId())
				.patientName(this.request.getPatientName())
				.doctorId(this.request.getDoctorId())
				.doctorName(this.request.getDoctorName())
				.department(this.request.getDepartment())
				.appointmentTime(this.request.getAppointmentTime())
				.durationMinutes(this.request.getDurationMinutes())
				.consultationFee(this.request.getConsultationFee())
				.reasonForVisit(this.request.getReasonForVisit())
				.status(AppointmentStatus.BOOKED)
				.createdBy("testuser")
				.isDeleted(false)
				.createdAt(LocalDateTime.now())
				.build();
	}

	@Test
	void createAppointment_Success() {
		// Given
		when(this.appointmentRepository.findOverlappingAppointments(
				anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
				.thenReturn(Collections.emptyList());
		when(this.appointmentRepository.save(any(Appointment.class)))
				.thenReturn(this.appointment);
		when(this.meterRegistry.counter(anyString(), any(String[].class)))
				.thenReturn(mock(io.micrometer.core.instrument.Counter.class));

		// When
		AppointmentResponse response = this.appointmentService
				.createAppointment(this.request, "testuser");

		// Then
		assertNotNull(response);
		assertEquals("APT-2025-ABC123", response.getAppointmentNumber());
		assertEquals("John Doe", response.getPatientName());
		assertEquals("BOOKED", response.getStatus());

		verify(this.appointmentRepository).save(any(Appointment.class));
		verify(this.kafkaProducerService).sendAppointmentBookedEvent(any());
	}

	@Test
	void createAppointment_OverlappingAppointment_ThrowsException() {
		// Given
		when(this.appointmentRepository.findOverlappingAppointments(
				anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
				.thenReturn(Collections.singletonList(this.appointment));

		// When & Then
		assertThrows(AppointmentConflictException.class, () -> this.appointmentService.createAppointment(this.request, "testuser"));

		verify(this.appointmentRepository, never()).save(any());
		verify(this.kafkaProducerService, never()).sendAppointmentBookedEvent(any());
	}

	@Test
	void getAppointment_Success() {
		// Given
		when(this.appointmentRepository.findById(1L))
				.thenReturn(Optional.of(this.appointment));

		// When
		AppointmentResponse response = this.appointmentService.getAppointment(1L);

		// Then
		assertNotNull(response);
		assertEquals(1L, response.getId());
		assertEquals("APT-2025-ABC123", response.getAppointmentNumber());
	}

	@Test
	void getAppointment_NotFound_ThrowsException() {
		// Given
		when(this.appointmentRepository.findById(1L))
				.thenReturn(Optional.empty());

		// When & Then
		assertThrows(ResourceNotFoundException.class, () -> this.appointmentService.getAppointment(1L));
	}

	@Test
	void createAppointment_PastTime_ThrowsException() {
		// Given
		this.request.setAppointmentTime(LocalDateTime.now().minusDays(1));

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> this.appointmentService.createAppointment(this.request, "testuser"));
	}

	@Test
	void createAppointment_OutsideBusinessHours_ThrowsException() {
		// Given
		this.request.setAppointmentTime(LocalDateTime.now().plusDays(1).withHour(20).withMinute(0));

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> this.appointmentService.createAppointment(this.request, "testuser"));
	}
}