package com.hospital.platform.appointment.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.hospital.platform.appointment.dto.AppointmentBookedEvent;
import com.hospital.platform.appointment.dto.AppointmentResponse;
import com.hospital.platform.appointment.dto.CreateAppointmentRequest;
import com.hospital.platform.appointment.entity.AppointmentEntity;
import com.hospital.platform.appointment.entity.AppointmentEntity.AppointmentStatus;

@Component
public class AppointmentMapper {

	public AppointmentEntity fromRequest(CreateAppointmentRequest request, String username) {

		AppointmentEntity appointment = new AppointmentEntity();
		appointment.setAppointmentNumber(generateAppointmentNumber());
		appointment.setPatientId(request.getPatientId());
		appointment.setPatientName(request.getPatientName());
		appointment.setDoctorId(request.getDoctorId());
		appointment.setDoctorName(request.getDoctorName());
		appointment.setDepartment(request.getDepartment());
		appointment.setAppointmentTime(request.getAppointmentTime());
		appointment.setDurationMinutes(request.getDurationMinutes());
		appointment.setConsultationFee(request.getConsultationFee());
		appointment.setReasonForVisit(request.getReasonForVisit());
		appointment.setNotes(request.getNotes());
		appointment.setStatus(AppointmentStatus.BOOKED);
		appointment.setCreatedBy(username);

		return appointment;

	}

	public AppointmentResponse toResponse(AppointmentEntity appointment) {
		return AppointmentResponse.builder()
				.id(appointment.getId())
				.appointmentNumber(appointment.getAppointmentNumber())
				.patientId(appointment.getPatientId())
				.patientName(appointment.getPatientName())
				.doctorId(appointment.getDoctorId())
				.doctorName(appointment.getDoctorName())
				.department(appointment.getDepartment())
				.appointmentTime(appointment.getAppointmentTime())
				.durationMinutes(appointment.getDurationMinutes())
				.status(appointment.getStatus().name())
				.consultationFee(appointment.getConsultationFee())
				.reasonForVisit(appointment.getReasonForVisit())
				.notes(appointment.getNotes())
				.cancellationReason(appointment.getCancellationReason())
				.cancelledAt(appointment.getCancelledAt())
				.checkedInAt(appointment.getCheckedInAt())
				.completedAt(appointment.getCompletedAt())
				.createdAt(appointment.getCreatedAt())
				.updatedAt(appointment.getUpdatedAt())
				.build();
	}

	public AppointmentBookedEvent fromAppointmentEntity(AppointmentEntity appointment) {
		return AppointmentBookedEvent.builder()
				.eventId(UUID.randomUUID().toString())
				.appointmentNumber(appointment.getAppointmentNumber())
				.appointmentId(appointment.getId())
				.patientId(appointment.getPatientId())
				.patientName(appointment.getPatientName())
				.doctorId(appointment.getDoctorId())
				.doctorName(appointment.getDoctorName())
				.department(appointment.getDepartment())
				.appointmentTime(appointment.getAppointmentTime())
				.consultationFee(appointment.getConsultationFee())
				.reasonForVisit(appointment.getReasonForVisit())
				.eventTimestamp(LocalDateTime.now())
				.createdBy(appointment.getCreatedBy())
				.build();
	}

	private String generateAppointmentNumber() {
		return "APT-" + LocalDateTime.now().getYear() + "-" +
				UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

}
