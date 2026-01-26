-- 25.01.2026

--tables

CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    appointment_number VARCHAR(50) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL,
    patient_name VARCHAR(255) NOT NULL,
    doctor_id BIGINT NOT NULL,
    doctor_name VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    appointment_time TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL DEFAULT 30,
    status TYPE appointment_status_enum USING status::appointment_status_enum,
    consultation_fee NUMERIC(10, 2) NOT NULL,
    reason_for_visit TEXT,
    notes TEXT,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT,
    cancellation_reason VARCHAR(500),
    cancelled_at TIMESTAMP,
    checked_in_at TIMESTAMP,
    completed_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

--INDEXES
CREATE INDEX idx_patient_id ON appointments (patient_id);
CREATE INDEX idx_doctor_id ON appointments (doctor_id);
CREATE INDEX idx_appointment_time ON appointments (appointment_time);
CREATE INDEX idx_status ON appointments (status);


--enums
CREATE TYPE appointment_status_enum AS ENUM (
  'BOOKED',
  'CHECKED_IN',
  'CANCELLED',
  'COMPLETED'
);