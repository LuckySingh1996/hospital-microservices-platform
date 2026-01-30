package com.hospital.platform.appointment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;

@Entity
@Table(name = "appointments")
@Data
public class AppointmentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "appointment_number", unique = true, nullable = false, length = 50)
	private String appointmentNumber;

	@Column(name = "patient_id", nullable = false)
	private Long patientId;

	@Column(name = "patient_name", nullable = false)
	private String patientName;

	@Column(name = "doctor_id", nullable = false)
	private Long doctorId;

	@Column(name = "doctor_name", nullable = false)
	private String doctorName;

	@Column(name = "department", nullable = false)
	private String department;

	@Column(name = "appointment_time", nullable = false)
	private LocalDateTime appointmentTime;

	@Column(name = "duration_minutes", nullable = false)
	private Integer durationMinutes;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private AppointmentStatus status;

	@Column(name = "consultation_fee", nullable = false, precision = 10, scale = 2)
	private BigDecimal consultationFee;

	@Column(name = "reason_for_visit", length = 500)
	private String reasonForVisit;

	@Column(name = "notes", length = 1000)
	private String notes;

	@Column(name = "created_by", nullable = false)
	private String createdBy;

	@Column(name = "updated_by")
	private String updatedBy;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Version
	@Column(name = "version")
	private Long version;

	@Column(name = "cancellation_reason", length = 500)
	private String cancellationReason;

	@Column(name = "cancelled_at")
	private LocalDateTime cancelledAt;

	@Column(name = "checked_in_at")
	private LocalDateTime checkedInAt;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted = false;

	public enum AppointmentStatus {
		BOOKED,
		CHECKED_IN,
		CANCELLED,
		COMPLETED
	}

	@PrePersist
	public void prePersist() {
		if (this.status == null) {
			this.status = AppointmentStatus.BOOKED;
		}
		if (this.durationMinutes == null) {
			this.durationMinutes = 30;
		}
	}
}
