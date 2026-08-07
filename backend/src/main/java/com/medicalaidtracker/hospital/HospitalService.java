package com.medicalaidtracker.hospital;

import com.medicalaidtracker.bed.BedRepository;
import com.medicalaidtracker.common.api.PageResponse;
import com.medicalaidtracker.common.domain.DomainEnums.AvailabilityStatus;
import com.medicalaidtracker.common.domain.DomainEnums.BedStatus;
import com.medicalaidtracker.common.domain.DomainEnums.WardType;
import com.medicalaidtracker.common.exception.ResourceNotFoundException;
import com.medicalaidtracker.doctor.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.medicalaidtracker.hospital.HospitalDtos.HospitalResponse;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final BedRepository bedRepository;
    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public PageResponse<HospitalResponse> search(String query, Pageable pageable) {
        return PageResponse.from(
                hospitalRepository.search(normalize(query), pageable),
                this::map
        );
    }

    @Transactional(readOnly = true)
    public HospitalResponse get(Long id) {
        return map(hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "HOSPITAL_NOT_FOUND", "Hospital not found.")));
    }

    @Transactional(readOnly = true)
    public PageResponse<HospitalResponse> emergency(
            WardType wardType,
            String query,
            Pageable pageable
    ) {
        return PageResponse.from(
                hospitalRepository.findWithEmergencyAvailability(
                        normalize(query),
                        wardType,
                        BedStatus.AVAILABLE,
                        AvailabilityStatus.AVAILABLE,
                        pageable
                ),
                this::map
        );
    }

    private HospitalResponse map(Hospital hospital) {
        return new HospitalResponse(
                hospital.getId(),
                hospital.getName(),
                hospital.getAddress(),
                hospital.getContactNumber(),
                bedRepository.countByHospitalIdAndWardTypeAndStatus(
                        hospital.getId(), WardType.ICU, BedStatus.AVAILABLE),
                bedRepository.countByHospitalIdAndWardTypeAndStatus(
                        hospital.getId(), WardType.GENERAL, BedStatus.AVAILABLE),
                doctorRepository.countByHospitalIdAndAvailabilityStatus(
                        hospital.getId(), AvailabilityStatus.AVAILABLE)
        );
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
