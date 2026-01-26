package com.hospital.billing.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.billing.dao.BillDao;
import com.hospital.billing.dto.AppointmentBookedEvent;
import com.hospital.billing.dto.BillGeneratedEvent;
import com.hospital.billing.dto.BillResponse;
import com.hospital.billing.dto.CreateBillRequest;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.exception.DuplicateBillException;
import com.hospital.billing.kafka.KafkaProducerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

	private final KafkaProducerService kafkaProducerService;
	private final BillDao dao;

	@Transactional
	public BillResponse createBill(CreateBillRequest request) {
		log.info("Creating bill for appointment: {}", request.getAppointmentId());

		// Prevent duplicate billing
		if (this.dao.existsByAppointmentId(request.getAppointmentId())) {
			throw new DuplicateBillException("Bill already exists for appointment: " + request.getAppointmentId());
		}

		Bill bill = Bill.builder()
				.billNumber(generateBillNumber())
				.appointmentId(request.getAppointmentId())
				.patientId(request.getPatientId())
				.patientName(request.getPatientName())
				.consultationFee(request.getConsultationFee())
				.labCharges(request.getLabCharges() != null ? request.getLabCharges() : BigDecimal.ZERO)
				.pharmacyCharges(request.getPharmacyCharges() != null ? request.getPharmacyCharges() : BigDecimal.ZERO)
				.paidAmount(BigDecimal.ZERO)
				.status(Bill.BillStatus.PENDING)
				.build();

		bill.calculateTotalAndDue();

		Bill savedBill = this.dao.save(bill);
		log.info("Bill created: {}", savedBill.getBillNumber());

		// Publish bill generated event
		publishBillGeneratedEvent(savedBill);

		return mapToResponse(savedBill);
	}

	@Transactional(readOnly = true)
	public BillResponse getBill(Long billId) {
		log.info("Fetching bill: {}", billId);

		Bill bill = this.dao.findByBillId(billId)
				.orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + billId));

		return mapToResponse(bill);
	}

	@Transactional
	public void updateBillPayment(Long billId, BigDecimal paidAmount) {
		log.info("Updating bill {} with payment: {}", billId, paidAmount);

		Bill bill = this.dao.findByBillId(billId)
				.orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + billId));

		bill.setPaidAmount(bill.getPaidAmount().add(paidAmount));
		bill.calculateTotalAndDue();

		this.dao.save(bill);
		log.info("Bill updated. Status: {}, Due: {}", bill.getStatus(), bill.getDueAmount());
	}

	public void handleAppointmentBooked(AppointmentBookedEvent event) {
		log.info("Received appointment booked event: {}", event.getAppointmentNumber());

		try {
			CreateBillRequest request = CreateBillRequest.builder()
					.appointmentId(event.getAppointmentId())
					.patientId(event.getPatientId())
					.patientName(event.getPatientName())
					.consultationFee(event.getConsultationFee())
					.labCharges(BigDecimal.ZERO)
					.pharmacyCharges(BigDecimal.ZERO)
					.build();

			createBill(request);
			log.info("Bill automatically generated for appointment: {}", event.getAppointmentId());

		} catch (DuplicateBillException e) {
			log.warn("Bill already exists for appointment: {}", event.getAppointmentId());
		} catch (Exception e) {
			log.error("Failed to generate bill for appointment: {}", event.getAppointmentId(), e);
			// Kafka will retry
			throw e;
		}
	}

	private String generateBillNumber() {
		return "BILL-" + LocalDateTime.now().getYear() + "-" +
				UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

	private void publishBillGeneratedEvent(Bill bill) {
		BillGeneratedEvent event = BillGeneratedEvent.builder()
				.eventId(UUID.randomUUID().toString())
				.billNumber(bill.getBillNumber())
				.billId(bill.getId())
				.appointmentId(bill.getAppointmentId())
				.patientId(bill.getPatientId())
				.totalAmount(bill.getTotalAmount())
				.eventTimestamp(LocalDateTime.now())
				.build();

		this.kafkaProducerService.sendBillGeneratedEvent(event);
	}

	private BillResponse mapToResponse(Bill bill) {
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
}