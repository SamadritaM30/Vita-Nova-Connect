package com.medicalaidtracker.representative;

import com.medicalaidtracker.appointment.AppointmentDtos.AppointmentCancelRequest;
import com.medicalaidtracker.appointment.AppointmentDtos.AppointmentResponse;
import com.medicalaidtracker.bed.BedDtos.BedResponse;
import com.medicalaidtracker.bed.BedDtos.BedStatusUpdateRequest;
import com.medicalaidtracker.common.api.ApiResponse;
import com.medicalaidtracker.common.api.PageResponse;
import com.medicalaidtracker.common.domain.DomainEnums.*;
import com.medicalaidtracker.doctor.DoctorDtos.DoctorAvailabilityUpdateRequest;
import com.medicalaidtracker.doctor.DoctorDtos.DoctorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static com.medicalaidtracker.common.api.PageablePolicy.allowSort;
import static com.medicalaidtracker.representative.RepresentativeDtos.RepresentativeDashboardResponse;

@RestController
@RequestMapping("/api/v1/representative")
@RequiredArgsConstructor
public class RepresentativeController {

    private final RepresentativeService representativeService;

    @GetMapping("/dashboard")
    ApiResponse<RepresentativeDashboardResponse> dashboard() {
        return ApiResponse.ok(representativeService.dashboard());
    }

    @GetMapping("/doctors")
    ApiResponse<PageResponse<DoctorResponse>> doctors(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) AvailabilityStatus availabilityStatus,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ApiResponse.ok(
                representativeService.doctors(
                        query,
                        hospitalId,
                        availabilityStatus,
                        allowSort(pageable, "id", "name", "specialization", "availabilityStatus")));
    }

    @PatchMapping("/doctors/{id}/availability")
    ApiResponse<DoctorResponse> updateDoctorAvailability(
            @PathVariable Long id,
            @Valid @RequestBody DoctorAvailabilityUpdateRequest request
    ) {
        return ApiResponse.ok(
                representativeService.updateDoctorAvailability(id, request.availabilityStatus()));
    }

    @GetMapping("/beds")
    ApiResponse<PageResponse<BedResponse>> beds(
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) WardType wardType,
            @RequestParam(required = false) BedStatus status,
            @PageableDefault(size = 20, sort = "bedCode") Pageable pageable
    ) {
        return ApiResponse.ok(representativeService.beds(
                hospitalId,
                wardType,
                status,
                allowSort(pageable, "id", "bedCode", "wardType", "status")));
    }

    @PatchMapping("/beds/{id}/status")
    ApiResponse<BedResponse> updateBedStatus(
            @PathVariable Long id,
            @Valid @RequestBody BedStatusUpdateRequest request
    ) {
        return ApiResponse.ok(representativeService.updateBedStatus(id, request.status()));
    }

    @GetMapping("/appointments")
    ApiResponse<PageResponse<AppointmentResponse>> appointments(
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startsAfter,
            @PageableDefault(size = 20, sort = "startsAt") Pageable pageable
    ) {
        return ApiResponse.ok(representativeService.appointments(
                hospitalId,
                status,
                dateFrom,
                dateTo,
                startsAfter,
                allowSort(pageable, "id", "startsAt", "status")));
    }

    @PatchMapping("/appointments/{id}/cancel")
    ApiResponse<AppointmentResponse> cancelAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentCancelRequest request
    ) {
        return ApiResponse.ok(representativeService.cancelAppointment(id, request));
    }
}
