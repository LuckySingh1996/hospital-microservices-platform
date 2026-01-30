package com.hospital.billing.service;

import static com.hospital.billing.entity.Bill.BillStatus.PAID;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.billing.dao.BillDao;
import com.hospital.billing.dto.AppointmentBookedEvent;
import com.hospital.billing.dto.BillGeneratedEvent;
import com.hospital.billing.dto.BillResponse;
import com.hospital.billing.dto.CreateBillRequest;
import com.hospital.billing.entity.Bill;
import com.hospital.billing.exception.ApplicationException;
import com.hospital.billing.exception.ErrorCode;
import com.hospital.billing.kafka.KafkaProducerService;
import com.hospital.billing.mapper.BillingMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

	private final KafkaProducerService kafkaProducerService;
	private final BillDao dao;
	private final BillingMapper mapper;

	@Transactional
	public BillResponse createBill(CreateBillRequest request) {
		log.info("Creating bill for appointment: {}", request.getAppointmentId());

		// Prevent duplicate billing
		if (this.dao.existsByAppointmentId(request.getAppointmentId())) {
			throw new ApplicationException(ErrorCode.DUPLICATE_BILL, "Bill already exists for appointment: " + request.getAppointmentId());
		}

		Bill bill = this.mapper.fromRequest(request);
		bill.calculateTotalAndDue();

		Bill savedBill = this.dao.save(bill);
		log.info("Bill created: {}", savedBill.getBillNumber());

		publishBillGeneratedEvent(savedBill);

		return mapToResponse(savedBill);
	}

	public BillResponse getBill(Long billId) {
		log.info("Fetching bill: {}", billId);

		Bill bill = this.dao.findByBillId(billId)
				.orElseThrow(() -> new ApplicationException(ErrorCode.BILL_NOT_FOUND, "Bill not found: " + billId));

		return mapToResponse(bill);
	}

	@Transactional
	public void updateBillPayment(Long billId, BigDecimal paidAmount) {
		log.info("Updating bill {} with payment: {}", billId, paidAmount);

		Bill bill = this.dao.findByIdForUpdate(billId)
				.orElseThrow(() -> new ApplicationException(ErrorCode.BILL_NOT_FOUND, "Bill not found: " + billId));

		if (bill.getStatus() == PAID) {
			throw new ApplicationException(ErrorCode.PAYMENT_FAILED, "Bill already paid");
		}

		bill.setPaidAmount(bill.getPaidAmount().add(paidAmount));
		bill.calculateTotalAndDue();

		log.info("Bill updated. Status: {}, Due: {}", bill.getStatus(), bill.getDueAmount());
	}

	@Transactional
	public void handleAppointmentBooked(AppointmentBookedEvent event) {
		log.info("Received appointment booked event: {}", event.getAppointmentNumber());

		try {
			CreateBillRequest request = this.mapper.fromApppointmentBookedEvent(event);
			createBillForConsumer(request);
			log.info("Bill automatically generated for appointment: {}", event.getAppointmentId());

		} catch (ApplicationException ex) {

			if (ex.getErrorCode() == ErrorCode.DUPLICATE_BILL) {
				log.warn("Bill already exists for appointment: {}", event.getAppointmentId());
				return;
			}

			log.error("Application error while generating bill for appointment {} [{}]", event.getAppointmentId(), ex.getErrorCode().name(), ex);
			throw ex;

		} catch (Exception ex) {

			log.error("Technical failure while generating bill for appointment {}", event.getAppointmentId(), ex);

			throw new ApplicationException(ErrorCode.INTERNAL_ERROR,
					"Unexpected failure while generating bill for appointment " + event.getAppointmentId());
		}

	}

	private Bill createBillForConsumer(CreateBillRequest request) {

		if (this.dao.existsByAppointmentId(request.getAppointmentId())) {
			throw new ApplicationException(
					ErrorCode.DUPLICATE_BILL,
					"Bill already exists for appointment");
		}

		Bill bill = this.mapper.fromRequest(request);
		bill.calculateTotalAndDue();
		return this.dao.save(bill);
	}

	private void publishBillGeneratedEvent(Bill bill) {
		BillGeneratedEvent event = this.mapper.fromBillEntity(bill);
		this.kafkaProducerService.sendBillGeneratedEvent(event);
	}

	private BillResponse mapToResponse(Bill bill) {
		return this.mapper.toResponse(bill);
	}
}