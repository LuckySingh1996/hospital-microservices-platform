package com.hospital.platform.appointment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.platform.appointment.dto.AppointmentResponse;
import com.hospital.platform.appointment.dto.CreateAppointmentRequest;
import com.hospital.platform.appointment.service.AppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

	private final AppointmentService appointmentService;

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
	public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody CreateAppointmentRequest request, Authentication authentication) {

		log.info("Received request to create appointment for patient: {}", request.getPatientId());

		String username = authentication.getName();
		AppointmentResponse response = this.appointmentService.createAppointment(request, username);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{appointmentId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
	public ResponseEntity<AppointmentResponse> getAppointment(
			@PathVariable Long appointmentId) {

		log.info("Received request to fetch appointment: {}", appointmentId);

		AppointmentResponse response = this.appointmentService.getAppointment(appointmentId);
		return ResponseEntity.ok(response);
	}
}