package com.medicalaidtracker.appointment;

import com.medicalaidtracker.common.api.ApiResponse;
import com.medicalaidtracker.common.api.PageResponse;
import com.medicalaidtracker.common.domain.DomainEnums.AppointmentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static com.medicalaidtracker.common.api.PageablePolicy.allowSort;
import static com.medicalaidtracker.appointment.AppointmentDtos.*;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    ApiResponse<PageResponse<AppointmentResponse>> list(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startsAfter,
            @PageableDefault(size = 20, sort = "startsAt") Pageable pageable
    ) {
        return ApiResponse.ok(appointmentService.listPatientAppointments(
                status,
                dateFrom,
                dateTo,
                startsAfter,
                allowSort(pageable, "id", "startsAt", "status")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<AppointmentResponse> create(@Valid @RequestBody AppointmentCreateRequest request) {
        return ApiResponse.ok(appointmentService.book(request));
    }

    @PatchMapping("/{id}/cancel")
    ApiResponse<AppointmentResponse> cancel(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentCancelRequest request
    ) {
        return ApiResponse.ok(appointmentService.cancelPatientAppointment(id, request));
    }
}
