package com.medicalaidtracker.hospital;

import com.medicalaidtracker.common.domain.DomainEnums.AvailabilityStatus;
import com.medicalaidtracker.common.domain.DomainEnums.BedStatus;
import com.medicalaidtracker.common.domain.DomainEnums.WardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    @Query("""
            select h from Hospital h
            where :query is null
               or lower(h.name) like lower(concat('%', :query, '%'))
               or lower(h.address) like lower(concat('%', :query, '%'))
            """)
    Page<Hospital> search(@Param("query") String query, Pageable pageable);

    @Query("""
            select h from Hospital h
            where (:query is null
                   or lower(h.name) like lower(concat('%', :query, '%'))
                   or lower(h.address) like lower(concat('%', :query, '%')))
              and exists (
                    select b.id from Bed b
                    where b.hospital = h
                      and b.wardType = :wardType
                      and b.status = :bedStatus
              )
              and exists (
                    select d.id from Doctor d
                    where d.hospital = h
                      and d.availabilityStatus = :doctorStatus
              )
            """)
    Page<Hospital> findWithEmergencyAvailability(
            @Param("query") String query,
            @Param("wardType") WardType wardType,
            @Param("bedStatus") BedStatus bedStatus,
            @Param("doctorStatus") AvailabilityStatus doctorStatus,
            Pageable pageable
    );
}
