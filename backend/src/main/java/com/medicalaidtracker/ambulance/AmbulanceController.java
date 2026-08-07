package com.medicalaidtracker.ambulance;

import com.medicalaidtracker.common.api.ApiResponse;
import com.medicalaidtracker.common.api.PageResponse;
import com.medicalaidtracker.common.domain.DomainEnums.AmbulanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import static com.medicalaidtracker.common.api.PageablePolicy.allowSort;
import static com.medicalaidtracker.ambulance.AmbulanceDtos.AmbulanceResponse;

@RestController
@RequestMapping("/api/v1/ambulances")
@RequiredArgsConstructor
public class AmbulanceController {

    private final AmbulanceService ambulanceService;

    @GetMapping
    ApiResponse<PageResponse<AmbulanceResponse>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "AVAILABLE") AmbulanceStatus status,
            @RequestParam(required = false) Long hospitalId,
            @PageableDefault(size = 20, sort = "lastLocation") Pageable pageable
    ) {
        return ApiResponse.ok(ambulanceService.search(
                query,
                status,
                hospitalId,
                allowSort(pageable, "id", "vehicleNumber", "lastLocation", "status")));
    }
}
