package com.hospital.platform.patient.service.validator;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.hospital.platform.patient.dto.PatientRequestDTO;
import com.hospital.platform.patient.exception.ApplicationException;
import com.hospital.platform.patient.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PatientValidationService {

	private static final String NAME_REGEX = "^[A-Za-z .'-]{2,50}$";

	public void validateForRegistration(PatientRequestDTO request) {
		log.info("Validating requestDTO for patient registration");
		validateDateOfBirth(request.getDateOfBirth());
		validateMobile(request.getMobile());
		validateEmergencyMobile(request.getMobile(), request.getEmergencyMobile());
		validateName(request.getFirstName(), "First Name");
		validateName(request.getLastName(), "Last Name");
		validateEmail(request.getEmail());
	}

	private void validateDateOfBirth(LocalDate dob) {
		if (dob.isAfter(LocalDate.now())) {
			log.warn("Invalid DOB provided");
			throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "Invalid DOB Provided");
		}
	}

	private void validateMobile(String mobile) {
		if (!mobile.matches("^[1-9][0-9]{9,14}$")) {
			log.warn("Invalid mobile number provided");
			throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "Invalid Mobile Provided");
		}
	}

	private void validateEmergencyMobile(String mobile, String emergencyMobile) {
		if (emergencyMobile != null) {
			if (!emergencyMobile.matches("^[1-9][0-9]{9,14}$")) {
				log.warn("Invalid emergency mobile number provided as : {}", emergencyMobile);
				throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "Invalid emergency mobile number format");
			}
			if (emergencyMobile.equals(mobile)) {
				log.warn("Emergency mobile number is same as primary mobile");
				throw new ApplicationException(
						ErrorCode.VALIDATION_ERROR, "Emergency mobile number must be different from primary mobile");
			}
		}
	}

	private void validateName(String name, String field) {
		if (!name.matches(NAME_REGEX)) {
			log.warn("Invalid {} provided", field);
			throw new ApplicationException(
					ErrorCode.VALIDATION_ERROR, "Invalid input for " + field);
		}
	}

	private void validateEmail(String email) {
		if (email != null && (!email.contains("@") || email.endsWith("@"))) {
			log.warn("Invalid email provided as : {}", email);
			throw new ApplicationException(ErrorCode.VALIDATION_ERROR, "Invalid Email Provided");
		}

	}

}
