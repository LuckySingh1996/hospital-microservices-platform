package com.hospital.platform.patient.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.platform.patient.dto.PatientRequestDTO;
import com.hospital.platform.patient.dto.PatientResponseDTO;
import com.hospital.platform.patient.service.PatientService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/patients")
@Slf4j
public class PatientController {

	private final PatientService patientService;

	public PatientController(PatientService patientService) {
		this.patientService = patientService;
	}

	@PostMapping
	public ResponseEntity<PatientResponseDTO> registerPatient(@Valid @RequestBody PatientRequestDTO request) {
		log.info("POST /patients registration request received");

		PatientResponseDTO response = this.patientService.registerPatient(request);
		log.info("Patient registered with hospitalPatientId={}", response.getHospitalPatientId());

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{hospitalPatientId}")
	public ResponseEntity<PatientResponseDTO> getPatientByHospitalPatientId(@PathVariable Integer hospitalPatientId) {
		log.info("GET /patients/{}", hospitalPatientId);

		PatientResponseDTO response = this.patientService.getPatientByHospitalPatientId(hospitalPatientId);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{hospitalPatientId}")
	public ResponseEntity<Void> deletePatient(@PathVariable Integer hospitalPatientId) {
		log.info("DELETE /patients/{}", hospitalPatientId);

		this.patientService.deletePatientByHospitalPatientId(hospitalPatientId);

		return ResponseEntity.noContent().build();
	}

}
