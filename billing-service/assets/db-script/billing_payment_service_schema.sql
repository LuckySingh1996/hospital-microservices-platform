-- 25.01.2026

-- =========================
-- Bills Table
-- =========================
CREATE TABLE IF NOT EXISTS bills (
    id BIGSERIAL PRIMARY KEY,

    bill_number VARCHAR(255) NOT NULL UNIQUE,
    appointment_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    patient_name VARCHAR(255) NOT NULL,

    consultation_fee NUMERIC(10, 2) NOT NULL CHECK (consultation_fee >= 0),
    lab_charges NUMERIC(10, 2) NOT NULL DEFAULT 0 CHECK (lab_charges >= 0),
    pharmacy_charges NUMERIC(10, 2) NOT NULL DEFAULT 0 CHECK (pharmacy_charges >= 0),

    total_amount NUMERIC(10, 2) NOT NULL CHECK (total_amount >= 0),
    paid_amount NUMERIC(10, 2) NOT NULL DEFAULT 0 CHECK (paid_amount >= 0),
    due_amount NUMERIC(10, 2) NOT NULL CHECK (due_amount >= 0),

    status VARCHAR(20) NOT NULL
        CHECK (status IN ('PENDING', 'PARTIALLY_PAID', 'PAID', 'CANCELLED')),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    version BIGINT,

    CONSTRAINT chk_due_vs_total
        CHECK (due_amount = total_amount - paid_amount)
);

-- Index (only what your API actually queries)
CREATE INDEX IF NOT EXISTS idx_bills_appointment_id
    ON bills (appointment_id);


-- =========================
-- Payments Table
-- =========================
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,

    payment_reference VARCHAR(255) NOT NULL UNIQUE,
    bill_id BIGINT NOT NULL,

    amount NUMERIC(10, 2) NOT NULL CHECK (amount >= 0),

    payment_method VARCHAR(20) NOT NULL
        CHECK (payment_method IN ('CASH', 'CARD', 'UPI', 'NET_BANKING')),

    status VARCHAR(20) NOT NULL
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),

    idempotency_key VARCHAR(255) NOT NULL UNIQUE,

    transaction_id VARCHAR(255),
    failure_reason TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_bill
        FOREIGN KEY (bill_id)
        REFERENCES bills (id)
        ON DELETE CASCADE
);

-- Index (purposeful only)
CREATE INDEX IF NOT EXISTS idx_payments_bill_id
    ON payments (bill_id);
