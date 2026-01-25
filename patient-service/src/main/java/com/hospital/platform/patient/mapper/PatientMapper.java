package com.hospital.platform.patient.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.hospital.platform.patient.dto.PatientRequestDTO;
import com.hospital.platform.patient.dto.PatientResponseDTO;
import com.hospital.platform.patient.entity.PatientEntity;

@Component
public class PatientMapper {

	public PatientEntity fromRequestDTO(PatientRequestDTO request) {
		PatientEntity entity = new PatientEntity();
		entity.setId(UUID.randomUUID().toString());
		entity.setFirstName(request.getFirstName());
		entity.setLastName(request.getLastName());
		entity.setDateOfBirth(request.getDateOfBirth());
		entity.setGender(request.getGender());
		entity.setEmail(request.getEmail());
		entity.setMobile(request.getMobile());
		entity.setAddress(request.getAddress());
		entity.setEmergencyMobile(request.getEmergencyMobile());
		entity.setCreatedOn(LocalDateTime.now());
		return entity;
	}

	public PatientResponseDTO fromPatientEntity(PatientEntity entity) {
		PatientResponseDTO response = new PatientResponseDTO();
		response.setHospitalPatientId(entity.getHospitalPatientId());
		response.setFirstName(entity.getFirstName());
		response.setLastName(entity.getLastName());
		response.setDateOfBirth(entity.getDateOfBirth());
		response.setGender(entity.getGender());
		response.setEmail(entity.getEmail());
		response.setMobile(entity.getMobile());
		response.setAddress(entity.getAddress());
		response.setEmergencyMobile(entity.getEmergencyMobile());
		return response;
	}

}
