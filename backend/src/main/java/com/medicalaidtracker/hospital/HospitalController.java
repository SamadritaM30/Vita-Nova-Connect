package com.medicalaidtracker.hospital;

import com.medicalaidtracker.common.api.ApiResponse;
import com.medicalaidtracker.common.api.PageResponse;
import com.medicalaidtracker.common.domain.DomainEnums.WardType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import static com.medicalaidtracker.common.api.PageablePolicy.allowSort;
import static com.medicalaidtracker.hospital.HospitalDtos.HospitalResponse;

@RestController
@RequestMapping("/api/v1/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping
    ApiResponse<PageResponse<HospitalResponse>> search(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ApiResponse.ok(hospitalService.search(
                query, allowSort(pageable, "id", "name")));
    }

    @GetMapping("/{id}")
    ApiResponse<HospitalResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(hospitalService.get(id));
    }

    @GetMapping("/emergency-availability")
    ApiResponse<PageResponse<HospitalResponse>> emergencyAvailability(
            @RequestParam WardType wardType,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return ApiResponse.ok(hospitalService.emergency(
                wardType, query, allowSort(pageable, "id", "name")));
    }
}
