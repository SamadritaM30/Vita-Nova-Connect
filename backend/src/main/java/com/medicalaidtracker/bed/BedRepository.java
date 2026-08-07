package com.medicalaidtracker.bed;

import com.medicalaidtracker.common.domain.DomainEnums.BedStatus;
import com.medicalaidtracker.common.domain.DomainEnums.WardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface BedRepository extends JpaRepository<Bed, Long> {

    long countByHospitalIdAndWardTypeAndStatus(
            Long hospitalId,
            WardType wardType,
            BedStatus status
    );

    long countByHospitalIdInAndStatus(Collection<Long> hospitalIds, BedStatus status);

    long countByHospitalIdInAndWardTypeAndStatus(
            Collection<Long> hospitalIds,
            WardType wardType,
            BedStatus status
    );

    long countByHospitalIdIn(Collection<Long> hospitalIds);

    @Query("""
            select b from Bed b
            where b.hospital.id in :hospitalIds
              and (:hospitalId is null or b.hospital.id = :hospitalId)
              and (:wardType is null or b.wardType = :wardType)
              and (:status is null or b.status = :status)
            """)
    Page<Bed> findAllForRepresentative(
            @Param("hospitalIds") Collection<Long> hospitalIds,
            @Param("hospitalId") Long hospitalId,
            @Param("wardType") WardType wardType,
            @Param("status") BedStatus status,
            Pageable pageable
    );
}
