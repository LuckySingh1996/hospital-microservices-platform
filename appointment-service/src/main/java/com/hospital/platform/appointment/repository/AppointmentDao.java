package com.hospital.platform.appointment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.hospital.platform.appointment.entity.AppointmentEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class AppointmentDao {

	@PersistenceContext
	private EntityManager em;

	public AppointmentEntity save(AppointmentEntity entity) {
		this.em.persist(entity);
		this.em.flush();
		return entity;
	}

	public Optional<AppointmentEntity> findByAppointmentId(Long id) {
		return this.em.createQuery(
				"from AppointmentEntity where id = :id and isDeleted = false", AppointmentEntity.class)
				.setParameter("id", id)
				.getResultList().stream().findFirst();
	}

	public List<AppointmentEntity> findOverlappingAppointments(
			Long doctorId,
			LocalDateTime startTime,
			LocalDateTime endTime) {

		String jpql = """
				SELECT a FROM AppointmentEntity a
				WHERE a.doctorId = :doctorId
				  AND a.isDeleted = false
				  AND a.status <> 'CANCELLED'
				  AND a.appointmentTime < :endTime
				  AND a.appointmentTime >= :earliestPossibleStart
				""";

		// Calculate earliest possible start (endTime - max duration)
		LocalDateTime earliestPossibleStart = endTime.minusHours(8); // Assume max 8hr appointments

		// Get potential overlaps, then filter in Java
		List<AppointmentEntity> potentialOverlaps = this.em
				.createQuery(jpql, AppointmentEntity.class)
				.setParameter("doctorId", doctorId)
				.setParameter("endTime", endTime)
				.setParameter("earliestPossibleStart", earliestPossibleStart)
				.getResultList();

		// Filter for actual overlaps
		return potentialOverlaps.stream()
				.filter(a -> {
					LocalDateTime appointmentEnd = a.getAppointmentTime()
							.plusMinutes(a.getDurationMinutes());
					return appointmentEnd.isAfter(startTime);
				})
				.toList();
	}

}
