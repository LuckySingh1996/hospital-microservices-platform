package com.hospital.billing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "bill_number", unique = true, nullable = false)
    private String billNumber;
    
    @Column(name = "appointment_id", nullable = false, unique = true)
    private Long appointmentId;
    
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    
    @Column(name = "patient_name", nullable = false)
    private String patientName;
    
    @Column(name = "consultation_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal consultationFee;
    
    @Column(name = "lab_charges", precision = 10, scale = 2)
    private BigDecimal labCharges;
    
    @Column(name = "pharmacy_charges", precision = 10, scale = 2)
    private BigDecimal pharmacyCharges;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount;
    
    @Column(name = "due_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal dueAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BillStatus status;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    public enum BillStatus {
        PENDING,
        PARTIALLY_PAID,
        PAID,
        CANCELLED
    }
    
    @PrePersist
    public void prePersist() {
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }
        if (labCharges == null) {
            labCharges = BigDecimal.ZERO;
        }
        if (pharmacyCharges == null) {
            pharmacyCharges = BigDecimal.ZERO;
        }
        calculateTotalAndDue();
    }
    
    public void calculateTotalAndDue() {
        totalAmount = consultationFee
            .add(labCharges)
            .add(pharmacyCharges);
        dueAmount = totalAmount.subtract(paidAmount);
        
        if (dueAmount.compareTo(BigDecimal.ZERO) == 0) {
            status = BillStatus.PAID;
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            status = BillStatus.PARTIALLY_PAID;
        } else {
            status = BillStatus.PENDING;
        }
    }
}