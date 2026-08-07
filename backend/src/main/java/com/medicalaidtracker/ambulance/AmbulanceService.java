package com.medicalaidtracker.ambulance;

import com.medicalaidtracker.audit.AuditService;
import com.medicalaidtracker.common.api.PageResponse;
import com.medicalaidtracker.common.domain.DomainEnums.AmbulanceRequestStatus;
import com.medicalaidtracker.common.domain.DomainEnums.AmbulanceStatus;
import com.medicalaidtracker.common.exception.ResourceNotFoundException;
import com.medicalaidtracker.config.AppProperties;
import com.medicalaidtracker.hospital.Hospital;
import com.medicalaidtracker.hospital.HospitalDtos.HospitalSummary;
import com.medicalaidtracker.hospital.HospitalRepository;
import com.medicalaidtracker.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static com.medicalaidtracker.ambulance.AmbulanceDtos.*;

@Service
@RequiredArgsConstructor
public class AmbulanceService {

    private final AmbulanceRepository ambulanceRepository;
    private final AmbulanceRequestRepository ambulanceRequestRepository;
    private final HospitalRepository hospitalRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final AppProperties properties;

    @Transactional(readOnly = true)
    public PageResponse<AmbulanceResponse> search(
            String query,
            AmbulanceStatus status,
            Long hospitalId,
            Pageable pageable
    ) {
        return PageResponse.from(
                ambulanceRepository.search(normalize(query), status, hospitalId, pageable),
                this::mapAmbulance
        );
    }

    @Transactional
    public AmbulanceRequestResponse request(AmbulanceRequestCreate request) {
        var patient = currentUserService.requirePatientProfile();
        Hospital hospital = hospitalRepository.findById(request.hospitalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "HOSPITAL_NOT_FOUND", "Hospital not found."));

        var ambulanceRequest = new AmbulanceRequest();
        ambulanceRequest.setPatient(patient);
        ambulanceRequest.setHospital(hospital);
        ambulanceRequest.setPickupAddress(request.pickupAddress().trim());
        ambulanceRequest.setContactNumber(request.contactNumber().trim());
        ambulanceRequest.setStatus(AmbulanceRequestStatus.PENDING);
        ambulanceRequest.setRequestedAt(Instant.now());
        ambulanceRequest = ambulanceRequestRepository.save(ambulanceRequest);

        auditService.record(
                patient.getUser(),
                "AMBULANCE_REQUESTED",
                "AmbulanceRequest",
                ambulanceRequest.getId(),
                "hospitalId=" + hospital.getId()
        );
        return mapRequest(ambulanceRequest);
    }

    @Transactional(readOnly = true)
    public PageResponse<AmbulanceRequestResponse> listPatientRequests(Pageable pageable) {
        var patient = currentUserService.requirePatientProfile();
        return PageResponse.from(
                ambulanceRequestRepository.findByPatientId(patient.getId(), pageable),
                this::mapRequest
        );
    }

    private AmbulanceResponse mapAmbulance(Ambulance ambulance) {
        return new AmbulanceResponse(
                ambulance.getId(),
                ambulance.getVehicleNumber(),
                ambulance.getDriverName(),
                ambulance.getContactNumber(),
                ambulance.getLastLocation(),
                ambulance.getStatus(),
                hospitalSummary(ambulance.getHospital())
        );
    }

    private AmbulanceRequestResponse mapRequest(AmbulanceRequest request) {
        return new AmbulanceRequestResponse(
                request.getId(),
                request.getStatus(),
                request.getPickupAddress(),
                request.getContactNumber(),
                request.getRequestedAt().atZone(properties.timeZone()).toOffsetDateTime(),
                hospitalSummary(request.getHospital()),
                request.getAmbulance() == null ? null : mapAmbulance(request.getAmbulance())
        );
    }

    private HospitalSummary hospitalSummary(Hospital hospital) {
        return new HospitalSummary(
                hospital.getId(), hospital.getName(), hospital.getAddress(), hospital.getContactNumber());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
