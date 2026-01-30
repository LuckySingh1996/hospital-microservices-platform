package com.hospital.platform.patient.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.hospital.platform.patient.entity.PatientEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class PatientDao {

	@PersistenceContext
	private EntityManager em;

	public PatientEntity save(PatientEntity entity) {
		this.em.persist(entity);
		return entity;
	}

	public Optional<PatientEntity> findByHospitalPatientId(Integer hospitalPatientId) {

		List<PatientEntity> result = this.em.createQuery(
				"from PatientEntity where hospitalPatientId = :id and isDeleted = false",
				PatientEntity.class)
				.setParameter("id", hospitalPatientId)
				.getResultList();

		return result.stream().findFirst();
	}

	public void softDelete(PatientEntity entity) {
		entity.setIsDeleted(true);
		this.em.merge(entity);
	}
}
