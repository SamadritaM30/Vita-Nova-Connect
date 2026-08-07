package com.medicalaidtracker.ambulance;

import com.medicalaidtracker.common.domain.DomainEnums.AmbulanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {

    @Query("""
            select a from Ambulance a
            where (:status is null or a.status = :status)
              and (:hospitalId is null or a.hospital.id = :hospitalId)
              and (:query is null
                   or lower(a.driverName) like lower(concat('%', :query, '%'))
                   or lower(a.lastLocation) like lower(concat('%', :query, '%'))
                   or lower(a.vehicleNumber) like lower(concat('%', :query, '%')))
            """)
    Page<Ambulance> search(
            @Param("query") String query,
            @Param("status") AmbulanceStatus status,
            @Param("hospitalId") Long hospitalId,
            Pageable pageable
    );
}
