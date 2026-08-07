package com.medicalaidtracker.common.api;

import com.medicalaidtracker.common.exception.BusinessRuleException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.Set;

public final class PageablePolicy {

    private PageablePolicy() {
    }

    public static Pageable allowSort(Pageable pageable, String... allowedProperties) {
        Set<String> allowed = Set.of(allowedProperties);
        pageable.getSort().forEach(order -> {
            if (!allowed.contains(order.getProperty())) {
                throw new BusinessRuleException(
                        "INVALID_SORT",
                        "Unsupported sort field: " + order.getProperty(),
                        HttpStatus.BAD_REQUEST
                );
            }
        });
        return pageable;
    }
}
