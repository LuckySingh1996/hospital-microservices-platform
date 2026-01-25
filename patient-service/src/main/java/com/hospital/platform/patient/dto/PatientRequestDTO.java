package com.hospital.platform.patient.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PatientRequestDTO {

	@NotBlank(message = "First name is required")
	private String firstName;

	@NotBlank(message = "Last name is required")
	private String lastName;

	@NotNull(message = "Date of birth is required")
	private LocalDate dateOfBirth;

	@NotBlank(message = "Gender is required")
	private String gender;

	@NotBlank(message = "Mobile is required")
	private String mobile;

	private String email;
	private String address;
	private String emergencyMobile;
}
