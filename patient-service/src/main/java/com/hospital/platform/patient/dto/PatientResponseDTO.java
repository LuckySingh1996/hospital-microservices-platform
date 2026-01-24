package com.hospital.platform.patient.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class PatientResponseDTO {

	private Long hospitalPatientId;

	private String firstName;
	private String lastName;
	private LocalDate dateOfBirth;
	private String gender;
	private String email;
	private String mobile;
	private String address;
	private String emergencyMobile;

}
