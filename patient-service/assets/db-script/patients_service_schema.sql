-- 24.01.2026

CREATE TABLE patients (
   
    id varchar(40) PRIMARY KEY,
    hospital_patient_id serial4 NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    email VARCHAR(100),
    mobile VARCHAR(15) NOT NULL,
    address TEXT,
    emergency_mobile VARCHAR(15),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP
);
	CREATE INDEX idx_hospital_patient_id ON patients(hospital_patient_id);
