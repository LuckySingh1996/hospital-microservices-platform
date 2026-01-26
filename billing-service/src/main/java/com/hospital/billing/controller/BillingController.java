package com.hospital.billing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.billing.dto.BillResponse;
import com.hospital.billing.dto.CreateBillRequest;
import com.hospital.billing.service.BillingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
@Slf4j
public class BillingController {

	private final BillingService billingService;

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
	public ResponseEntity<BillResponse> createBill(@Valid @RequestBody CreateBillRequest request) {
		log.info("Request to create bill for appointment: {}", request.getAppointmentId());
		BillResponse response = this.billingService.createBill(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{billId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
	public ResponseEntity<BillResponse> getBill(@PathVariable("billId") Long billId) {
		log.info("Request to fetch bill: {}", billId);
		BillResponse response = this.billingService.getBill(billId);
		return ResponseEntity.ok(response);
	}

}