package com.hospital.platform.patient.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "patients")
@Data
public class PatientEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "hospital_patient_id", length = 50, nullable = false, updatable = false)
	private Long hospitalPatientId;

	@Column(name = "id", length = 50, nullable = false, updatable = false)
	private String id;

	@Column(name = "first_name", nullable = false, length = 50)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 50)
	private String lastName;

	@Column(name = "date_of_birth", nullable = false)
	private LocalDate dateOfBirth;

	@Column(name = "gender", nullable = false, length = 10)
	private String gender;

	@Column(name = "email", length = 100)
	private String email;

	@Column(name = "mobile", nullable = false, length = 15)
	private String mobile;

	@Column(name = "address", columnDefinition = "TEXT")
	private String address;

	@Column(name = "emergency_mobile", length = 15)
	private String emergencyMobile;

	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted = false;

	@Column(name = "created_on", nullable = false, updatable = false)
	private LocalDateTime createdOn;

	@Column(name = "updated_on")
	private LocalDateTime updatedOn;

}