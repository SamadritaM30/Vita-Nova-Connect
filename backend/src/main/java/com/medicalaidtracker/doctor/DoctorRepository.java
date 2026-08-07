package com.medicalaidtracker.doctor;

import com.medicalaidtracker.common.domain.DomainEnums.AvailabilityStatus;
import com.medicalaidtracker.common.domain.DomainEnums.BedStatus;
import com.medicalaidtracker.common.domain.DomainEnums.WardType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @Query("""
            select d from Doctor d
            join fetch d.hospital
            where d.id = :id
            """)
    Optional<Doctor> findDetailedById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Doctor d join fetch d.hospital where d.id = :id")
    Optional<Doctor> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select d from Doctor d
            where (:query is null
                   or lower(d.name) like lower(concat('%', :query, '%'))
                   or lower(d.specialization) like lower(concat('%', :query, '%'))
                   or lower(d.hospital.name) like lower(concat('%', :query, '%')))
              and (:specialization is null or lower(d.specialization) = lower(:specialization))
              and (:hospitalId is null or d.hospital.id = :hospitalId)
              and (:availabilityStatus is null or d.availabilityStatus = :availabilityStatus)
              and (:wardType is null or exists (
                    select b.id from Bed b
                    where b.hospital = d.hospital
                      and b.wardType = :wardType
                      and b.status = :bedStatus
              ))
            """)
    Page<Doctor> search(
            @Param("query") String query,
            @Param("specialization") String specialization,
            @Param("hospitalId") Long hospitalId,
            @Param("availabilityStatus") AvailabilityStatus availabilityStatus,
            @Param("wardType") WardType wardType,
            @Param("bedStatus") BedStatus bedStatus,
            Pageable pageable
    );

    @Query("""
            select d from Doctor d
            where d.hospital.id in :hospitalIds
              and (:hospitalId is null or d.hospital.id = :hospitalId)
              and (:availabilityStatus is null or d.availabilityStatus = :availabilityStatus)
              and (:query is null
                   or lower(d.name) like lower(concat('%', :query, '%'))
                   or lower(d.specialization) like lower(concat('%', :query, '%')))
            """)
    Page<Doctor> findAllForRepresentative(
            @Param("hospitalIds") Collection<Long> hospitalIds,
            @Param("hospitalId") Long hospitalId,
            @Param("availabilityStatus") AvailabilityStatus availabilityStatus,
            @Param("query") String query,
            Pageable pageable
    );

    long countByHospitalIdInAndAvailabilityStatus(
            Collection<Long> hospitalIds,
            AvailabilityStatus availabilityStatus
    );

    long countByHospitalIdIn(Collection<Long> hospitalIds);

    long countByHospitalIdAndAvailabilityStatus(Long hospitalId, AvailabilityStatus availabilityStatus);
}
