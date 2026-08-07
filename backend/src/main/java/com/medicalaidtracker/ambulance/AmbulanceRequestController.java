package com.medicalaidtracker.ambulance;

import com.medicalaidtracker.common.api.ApiResponse;
import com.medicalaidtracker.common.api.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.medicalaidtracker.common.api.PageablePolicy.allowSort;
import static com.medicalaidtracker.ambulance.AmbulanceDtos.*;

@RestController
@RequestMapping("/api/v1/ambulance-requests")
@RequiredArgsConstructor
public class AmbulanceRequestController {

    private final AmbulanceService ambulanceService;

    @GetMapping
    ApiResponse<PageResponse<AmbulanceRequestResponse>> list(
            @PageableDefault(
                    size = 20,
                    sort = "requestedAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ApiResponse.ok(ambulanceService.listPatientRequests(
                allowSort(pageable, "id", "requestedAt", "status")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<AmbulanceRequestResponse> create(
            @Valid @RequestBody AmbulanceRequestCreate request
    ) {
        return ApiResponse.ok(ambulanceService.request(request));
    }
}
