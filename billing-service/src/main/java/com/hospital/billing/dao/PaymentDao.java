package com.hospital.billing.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.hospital.billing.entity.Payment;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class PaymentDao {

	@PersistenceContext
	private EntityManager em;

	public Payment save(Payment entity) {
		this.em.persist(entity);
		return entity;
	}

	public boolean existsByIdempotencyKey(String idempotencyKey) {
		List<Payment> resultList = this.em.createQuery("from Payment where idempotencyKey = :idempotencyKey", Payment.class)
				.setParameter("idempotencyKey", idempotencyKey)
				.getResultList();
		return !resultList.isEmpty();
	}

}
