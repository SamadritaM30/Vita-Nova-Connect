package com.medicalaidtracker.ambulance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AmbulanceRequestRepository extends JpaRepository<AmbulanceRequest, UUID> {
    Page<AmbulanceRequest> findByPatientId(UUID patientId, Pageable pageable);
}
