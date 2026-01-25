package com.hospital.platform.appointment.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hospital.platform.appointment.entity.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	@Query(value = """
			SELECT *
			FROM appointments a
			WHERE a.doctor_id = ?1
			  AND a.appointment_time < ?3
			  AND (a.appointment_time + (a.duration_minutes || ' minutes')::interval) > ?2
			  AND a.is_deleted = false
			  AND a.status <> 'CANCELLED'
			""", nativeQuery = true)
	List<Appointment> findOverlappingAppointments(
			Long doctorId,
			LocalDateTime startTime,
			LocalDateTime endTime);

	/*@Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId " +
			"AND DATE(a.appointmentTime) = DATE(:date) " +
			"AND a.status != 'CANCELLED' " +
			"AND a.isDeleted = false " +
			"ORDER BY a.appointmentTime ASC")
	List<Appointment> findDoctorAppointmentsForDate(
			@Param("doctorId") Long doctorId,
			@Param("date") LocalDateTime date);*/

	/*@Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status " +
			"AND a.createdAt >= :fromDate")
	long countAppointmentsByStatusSince(
			@Param("status") AppointmentStatus status,
			@Param("fromDate") LocalDateTime fromDate);*/
}