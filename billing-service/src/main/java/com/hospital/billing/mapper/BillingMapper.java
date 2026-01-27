package com.hospital.billing.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.hospital.billing.dto.AppointmentBookedEvent;
import com.hospital.billing.dto.BillGeneratedEvent;
import com.hospital.billing.dto.BillResponse;
import com.hospital.billing.dto.CreateBillRequest;
import com.hospital.billing.entity.Bill;

@Component
public class BillingMapper {

	public BillResponse toResponse(Bill bill) {
		return BillResponse.builder()
				.id(bill.getId())
				.billNumber(bill.getBillNumber())
				.appointmentId(bill.getAppointmentId())
				.patientId(bill.getPatientId())
				.patientName(bill.getPatientName())
				.consultationFee(bill.getConsultationFee())
				.labCharges(bill.getLabCharges())
				.pharmacyCharges(bill.getPharmacyCharges())
				.totalAmount(bill.getTotalAmount())
				.paidAmount(bill.getPaidAmount())
				.dueAmount(bill.getDueAmount())
				.status(bill.getStatus().name())
				.createdAt(bill.getCreatedAt())
				.updatedAt(bill.getUpdatedAt())
				.build();
	}

	public BillGeneratedEvent fromBillEntity(Bill bill) {
		return BillGeneratedEvent.builder()
				.eventId(UUID.randomUUID().toString())
				.billNumber(bill.getBillNumber())
				.billId(bill.getId())
				.appointmentId(bill.getAppointmentId())
				.patientId(bill.getPatientId())
				.totalAmount(bill.getTotalAmount())
				.eventTimestamp(LocalDateTime.now())
				.build();
	}

	public CreateBillRequest fromApppointmentBookedEvent(AppointmentBookedEvent event) {
		return CreateBillRequest.builder()
				.appointmentId(event.getAppointmentId())
				.patientId(event.getPatientId())
				.patientName(event.getPatientName())
				.consultationFee(event.getConsultationFee())
				.labCharges(BigDecimal.ZERO)
				.pharmacyCharges(BigDecimal.ZERO)
				.build();
	}

	public Bill fromRequest(CreateBillRequest request) {

		Bill bill = new Bill();

		bill.setBillNumber(generateBillNumber());
		bill.setAppointmentId(request.getAppointmentId());
		bill.setPatientId(request.getPatientId());
		bill.setPatientName(request.getPatientName());
		bill.setConsultationFee(request.getConsultationFee());
		bill.setLabCharges(request.getLabCharges() != null ? request.getLabCharges() : BigDecimal.ZERO);
		bill.setPharmacyCharges(request.getPharmacyCharges() != null ? request.getPharmacyCharges() : BigDecimal.ZERO);
		bill.setPaidAmount(BigDecimal.ZERO);
		bill.setStatus(Bill.BillStatus.PENDING);
		return bill;

	}

	private String generateBillNumber() {
		return "BILL-" + LocalDateTime.now().getYear() + "-" +
				UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

}
