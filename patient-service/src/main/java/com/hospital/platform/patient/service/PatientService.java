package com.hospital.platform.patient.service;

import org.springframework.stereotype.Service;

import com.hospital.platform.patient.dao.PatientDao;
import com.hospital.platform.patient.dto.PatientRequestDTO;
import com.hospital.platform.patient.dto.PatientResponseDTO;
import com.hospital.platform.patient.entity.PatientEntity;
import com.hospital.platform.patient.exception.ApplicationException;
import com.hospital.platform.patient.exception.ErrorCode;
import com.hospital.platform.patient.mapper.PatientMapper;
import com.hospital.platform.patient.service.validator.PatientValidationService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PatientService {

	private final PatientDao patientDao;
	private final PatientMapper patientMapper;
	private final PatientValidationService validationService;

	public PatientService(PatientDao patientDao, PatientMapper patientMapper, PatientValidationService validationService) {
		this.patientDao = patientDao;
		this.patientMapper = patientMapper;
		this.validationService = validationService;
	}

	@Transactional
	public PatientResponseDTO registerPatient(PatientRequestDTO request) {
		log.info("Registering new patient");

		this.validationService.validateForRegistration(request);

		return this.patientMapper.fromPatientEntity(
				this.patientDao.save(
						this.patientMapper.fromRequestDTO(request)));
	}

	public PatientResponseDTO getPatientByHospitalPatientId(Integer hospitalPatientId) {
		log.info("Going to get pateint details with hospitalPatientId : {}", hospitalPatientId);
		return this.patientMapper.fromPatientEntity(this.patientDao.findByHospitalPatientId(hospitalPatientId)
				.orElseThrow(() -> new ApplicationException(ErrorCode.PATIENT_NOT_FOUND)));
	}

	@Transactional
	public void deletePatientByHospitalPatientId(Integer hospitalPatientId) {
		log.info("Going to delete patient with hospitalPatientId : {}", hospitalPatientId);

		PatientEntity patient = this.patientDao.findByHospitalPatientId(hospitalPatientId)
				.orElseThrow(() -> new ApplicationException(ErrorCode.PATIENT_NOT_FOUND));

		if (Boolean.TRUE.equals(patient.getIsDeleted())) {
			throw new ApplicationException(ErrorCode.PATIENT_ALREADY_DELETED);
		}

		this.patientDao.softDelete(patient);
		log.info("Patient with hospitalPatientId : {} is deleted successfully", hospitalPatientId);
	}

}
