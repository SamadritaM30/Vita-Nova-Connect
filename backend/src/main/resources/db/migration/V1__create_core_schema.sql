CREATE TABLE hospitals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(180) NOT NULL,
    address VARCHAR(500) NOT NULL,
    contact_number VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE users (
    id CHAR(36) PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    avatar_url VARCHAR(1000),
    provider VARCHAR(30) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_provider_subject UNIQUE (provider_subject)
);

CREATE TABLE patient_profiles (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    date_of_birth DATE,
    contact_number VARCHAR(20),
    blood_group VARCHAR(5),
    address VARCHAR(500),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_patient_profiles_user UNIQUE (user_id),
    CONSTRAINT fk_patient_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE representative_profiles (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    employee_id VARCHAR(80),
    organization_name VARCHAR(160),
    contact_number VARCHAR(20),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_representative_profiles_user UNIQUE (user_id),
    CONSTRAINT fk_representative_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE representative_invitations (
    id CHAR(36) PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    hospital_id BIGINT NOT NULL,
    employee_id VARCHAR(80),
    organization_name VARCHAR(160),
    accepted_at TIMESTAMP(6),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_representative_invitation UNIQUE (email, hospital_id),
    CONSTRAINT fk_representative_invitations_hospital
        FOREIGN KEY (hospital_id) REFERENCES hospitals (id)
);

CREATE TABLE representative_hospitals (
    id CHAR(36) PRIMARY KEY,
    representative_profile_id CHAR(36) NOT NULL,
    hospital_id BIGINT NOT NULL,
    CONSTRAINT uk_representative_hospital UNIQUE (representative_profile_id, hospital_id),
    CONSTRAINT fk_representative_hospitals_profile
        FOREIGN KEY (representative_profile_id) REFERENCES representative_profiles (id),
    CONSTRAINT fk_representative_hospitals_hospital
        FOREIGN KEY (hospital_id) REFERENCES hospitals (id)
);

CREATE TABLE doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    specialization VARCHAR(120) NOT NULL,
    availability_status VARCHAR(30) NOT NULL,
    hospital_id BIGINT NOT NULL,
    consultation_from TIME NOT NULL,
    consultation_to TIME NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_doctors_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id)
);

CREATE TABLE beds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hospital_id BIGINT NOT NULL,
    bed_code VARCHAR(40) NOT NULL,
    ward_type VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_beds_hospital_code UNIQUE (hospital_id, bed_code),
    CONSTRAINT fk_beds_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id)
);

CREATE TABLE ambulances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hospital_id BIGINT,
    vehicle_number VARCHAR(30) NOT NULL,
    driver_name VARCHAR(120) NOT NULL,
    contact_number VARCHAR(30) NOT NULL,
    last_location VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_ambulances_vehicle_number UNIQUE (vehicle_number),
    CONSTRAINT fk_ambulances_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id)
);

CREATE TABLE appointments (
    id CHAR(36) PRIMARY KEY,
    patient_profile_id CHAR(36) NOT NULL,
    doctor_id BIGINT NOT NULL,
    hospital_id BIGINT NOT NULL,
    starts_at TIMESTAMP(6) NOT NULL,
    ends_at TIMESTAMP(6) NOT NULL,
    status VARCHAR(30) NOT NULL,
    cancellation_reason VARCHAR(500),
    cancelled_at TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_appointments_patient
        FOREIGN KEY (patient_profile_id) REFERENCES patient_profiles (id),
    CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_appointments_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals (id)
);

CREATE TABLE ambulance_requests (
    id CHAR(36) PRIMARY KEY,
    patient_profile_id CHAR(36) NOT NULL,
    hospital_id BIGINT NOT NULL,
    ambulance_id BIGINT,
    pickup_address VARCHAR(500) NOT NULL,
    contact_number VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    requested_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_ambulance_requests_patient
        FOREIGN KEY (patient_profile_id) REFERENCES patient_profiles (id),
    CONSTRAINT fk_ambulance_requests_hospital
        FOREIGN KEY (hospital_id) REFERENCES hospitals (id),
    CONSTRAINT fk_ambulance_requests_ambulance
        FOREIGN KEY (ambulance_id) REFERENCES ambulances (id)
);

CREATE TABLE audit_logs (
    id CHAR(36) PRIMARY KEY,
    actor_user_id CHAR(36),
    action VARCHAR(80) NOT NULL,
    resource_type VARCHAR(80) NOT NULL,
    resource_id VARCHAR(80) NOT NULL,
    details VARCHAR(2000),
    ip_address VARCHAR(64),
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_audit_logs_actor FOREIGN KEY (actor_user_id) REFERENCES users (id)
);

CREATE INDEX idx_doctors_specialization ON doctors (specialization);
CREATE INDEX idx_doctors_hospital_status ON doctors (hospital_id, availability_status);
CREATE INDEX idx_beds_hospital_ward_status ON beds (hospital_id, ward_type, status);
CREATE INDEX idx_appointments_patient_start ON appointments (patient_profile_id, starts_at);
CREATE INDEX idx_appointments_doctor_start_status ON appointments (doctor_id, starts_at, status);
CREATE INDEX idx_appointments_hospital_start ON appointments (hospital_id, starts_at);
CREATE INDEX idx_ambulances_status ON ambulances (status);
CREATE INDEX idx_ambulance_requests_patient ON ambulance_requests (patient_profile_id, requested_at);
CREATE INDEX idx_audit_resource ON audit_logs (resource_type, resource_id, created_at);

CREATE TABLE SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES BLOB NOT NULL,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK
        FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION (PRIMARY_ID) ON DELETE CASCADE
);
