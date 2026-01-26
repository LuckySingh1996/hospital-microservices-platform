-- ============================================================================
-- Hospital Appointment Service - Database Initialization Script
-- ============================================================================

-- Create appointments table
CREATE TABLE IF NOT EXISTS appointments (
    id BIGSERIAL PRIMARY KEY,
    appointment_number VARCHAR(50) UNIQUE NOT NULL,
    patient_id BIGINT NOT NULL,
    patient_name VARCHAR(100) NOT NULL,
    doctor_id BIGINT NOT NULL,
    doctor_name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    appointment_time TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL DEFAULT 30,
    status VARCHAR(20) NOT NULL,
    consultation_fee DECIMAL(10,2) NOT NULL,
    reason_for_visit VARCHAR(500),
    notes VARCHAR(1000),
    cancellation_reason VARCHAR(500),
    cancelled_at TIMESTAMP,
    checked_in_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_patient_id ON appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_doctor_id ON appointments(doctor_id);
CREATE INDEX IF NOT EXISTS idx_appointment_time ON appointments(appointment_time);
CREATE INDEX IF NOT EXISTS idx_status ON appointments(status);
CREATE INDEX IF NOT EXISTS idx_appointment_number ON appointments(appointment_number);
CREATE INDEX IF NOT EXISTS idx_deleted ON appointments(is_deleted);

-- Create composite index for overlapping check
CREATE INDEX IF NOT EXISTS idx_doctor_time ON appointments(doctor_id, appointment_time);

-- Function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger to auto-update updated_at
CREATE TRIGGER update_appointments_updated_at 
    BEFORE UPDATE ON appointments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add table comments
COMMENT ON TABLE appointments IS 'Stores all patient appointment information';
COMMENT ON COLUMN appointments.appointment_number IS 'Unique appointment identifier in format APT-YEAR-XXXXXXXX';
COMMENT ON COLUMN appointments.status IS 'Appointment status: BOOKED, CHECKED_IN, CANCELLED, COMPLETED';
COMMENT ON COLUMN appointments.is_deleted IS 'Soft delete flag - true means logically deleted';

-- Insert sample data for testing (optional - comment out for production)
INSERT INTO appointments (
    appointment_number, patient_id, patient_name, doctor_id, doctor_name,
    department, appointment_time, duration_minutes, status, consultation_fee,
    reason_for_visit, created_by, is_deleted
) VALUES
    ('APT-2025-SAMPLE01', 1, 'John Doe', 1, 'Dr. Sarah Smith', 'Cardiology',
     '2025-02-15 10:00:00', 30, 'BOOKED', 500.00, 'Regular heart checkup', 'system', false),
    ('APT-2025-SAMPLE02', 2, 'Jane Wilson', 2, 'Dr. Michael Johnson', 'Orthopedics',
     '2025-02-15 11:00:00', 30, 'BOOKED', 600.00, 'Knee pain consultation', 'system', false),
    ('APT-2025-SAMPLE03', 3, 'Robert Brown', 1, 'Dr. Sarah Smith', 'Cardiology',
     '2025-02-16 14:00:00', 45, 'CHECKED_IN', 500.00, 'Follow-up checkup', 'system', false)
ON CONFLICT (appointment_number) DO NOTHING;

-- Create view for active appointments
CREATE OR REPLACE VIEW active_appointments AS
SELECT * FROM appointments
WHERE is_deleted = FALSE
ORDER BY appointment_time;

COMMENT ON VIEW active_appointments IS 'View of all non-deleted appointments';
