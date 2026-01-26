package com.hospital.billing.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.hospital.billing.entity.Bill;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class BillDao {

	@PersistenceContext
	private EntityManager em;

	public Bill save(Bill entity) {
		this.em.persist(entity);
		this.em.flush();
		return entity;
	}

	public boolean existsByAppointmentId(Long appointmentId) {
		List<Bill> resultList = this.em.createQuery("from Bill where appointmentId = :appointmentId", Bill.class)
				.setParameter("appointmentId", appointmentId)
				.getResultList();
		return !resultList.isEmpty();
	}

	public Optional<Bill> findByBillId(Long billId) {
		return this.em.createQuery("from Bill where id = :billId", Bill.class)
				.setParameter("billId", billId)
				.getResultList().stream().findFirst();
	}

}
