package com.medicalaidtracker.doctor;

import com.medicalaidtracker.appointment.AppointmentService;
import com.medicalaidtracker.common.api.ApiResponse;
import com.medicalaidtracker.common.api.PageResponse;
import com.medicalaidtracker.common.domain.DomainEnums.AvailabilityStatus;
import com.medicalaidtracker.common.domain.DomainEnums.WardType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.medicalaidtracker.common.api.PageablePolicy.allowSort;
import static com.medicalaidtracker.doctor.DoctorDtos.*;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @GetMapping
    ApiResponse<PageResponse<DoctorResponse>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) AvailabilityStatus availabilityStatus,
            @RequestParam(required = false) WardType wardType,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ApiResponse.ok(doctorService.search(
                query, specialization, hospitalId, availabilityStatus, wardType,
                allowSort(pageable, "id", "name", "specialization", "availabilityStatus")));
    }

    @GetMapping("/{id}")
    ApiResponse<DoctorResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(doctorService.get(id));
    }

    @GetMapping("/{id}/slots")
    ApiResponse<List<AppointmentSlotResponse>> slots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(appointmentService.availableSlots(id, date));
    }
}
