package com.hospital.billing.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.billing.entity.Bill;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

	Optional<Bill> findByAppointmentId(Long appointmentId);

	boolean existsByAppointmentId(Long appointmentId);
}