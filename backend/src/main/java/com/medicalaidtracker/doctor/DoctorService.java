package com.medicalaidtracker.doctor;

import com.medicalaidtracker.common.api.PageResponse;
import com.medicalaidtracker.common.domain.DomainEnums.AvailabilityStatus;
import com.medicalaidtracker.common.domain.DomainEnums.BedStatus;
import com.medicalaidtracker.common.domain.DomainEnums.WardType;
import com.medicalaidtracker.common.exception.ResourceNotFoundException;
import com.medicalaidtracker.hospital.HospitalDtos.HospitalSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.medicalaidtracker.doctor.DoctorDtos.DoctorResponse;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public PageResponse<DoctorResponse> search(
            String query,
            String specialization,
            Long hospitalId,
            AvailabilityStatus availabilityStatus,
            WardType wardType,
            Pageable pageable
    ) {
        return PageResponse.from(
                doctorRepository.search(
                        normalize(query),
                        normalize(specialization),
                        hospitalId,
                        availabilityStatus,
                        wardType,
                        BedStatus.AVAILABLE,
                        pageable
                ),
                this::map
        );
    }

    @Transactional(readOnly = true)
    public DoctorResponse get(Long id) {
        return map(requireDoctor(id));
    }

    @Transactional(readOnly = true)
    public Doctor requireDoctor(Long id) {
        return doctorRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DOCTOR_NOT_FOUND", "Doctor not found."));
    }

    public DoctorResponse map(Doctor doctor) {
        var hospital = doctor.getHospital();
        return new DoctorResponse(
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialization(),
                doctor.getAvailabilityStatus(),
                doctor.getConsultationFrom(),
                doctor.getConsultationTo(),
                new HospitalSummary(
                        hospital.getId(),
                        hospital.getName(),
                        hospital.getAddress(),
                        hospital.getContactNumber()
                )
        );
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
