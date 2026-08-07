package com.medicalaidtracker.appointment;

import com.medicalaidtracker.common.domain.DomainEnums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query("""
            select (count(a) > 0) from Appointment a
            where a.doctor.id = :doctorId
              and a.status = :status
              and a.startsAt < :endsAt
              and a.endsAt > :startsAt
            """)
    boolean existsOverlapping(
            @Param("doctorId") Long doctorId,
            @Param("startsAt") Instant startsAt,
            @Param("endsAt") Instant endsAt,
            @Param("status") AppointmentStatus status
    );

    @Query("""
            select a from Appointment a
            where a.doctor.id = :doctorId
              and a.status = :status
              and a.startsAt >= :from
              and a.startsAt < :to
            order by a.startsAt
            """)
    List<Appointment> findDoctorSchedule(
            @Param("doctorId") Long doctorId,
            @Param("status") AppointmentStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
            select a from Appointment a
            where a.patient.id = :patientId
              and (
                    :status is null
                    or (:status = :cancelledStatus
                        and a.status = :cancelledStatus)
                    or (:status = :completedStatus
                        and (
                            a.status = :completedStatus
                            or (
                                a.status = :bookedStatus
                                and a.endsAt <= :now
                            )
                        ))
                    or (:status = :bookedStatus
                        and a.status = :bookedStatus
                        and a.endsAt > :now)
                  )
              and (:from is null or a.startsAt >= :from)
              and (:to is null or a.startsAt < :to)
            """)
    Page<Appointment> findAllForPatient(
            @Param("patientId") UUID patientId,
            @Param("status") AppointmentStatus status,
            @Param("bookedStatus") AppointmentStatus bookedStatus,
            @Param("completedStatus") AppointmentStatus completedStatus,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus,
            @Param("now") Instant now,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    Optional<Appointment> findByIdAndPatientId(UUID id, UUID patientId);

    @Query("""
            select a from Appointment a
            where a.hospital.id in :hospitalIds
              and (:hospitalId is null or a.hospital.id = :hospitalId)
              and (
                    :status is null
                    or (:status = :cancelledStatus
                        and a.status = :cancelledStatus)
                    or (:status = :completedStatus
                        and (
                            a.status = :completedStatus
                            or (
                                a.status = :bookedStatus
                                and a.endsAt <= :now
                            )
                        ))
                    or (:status = :bookedStatus
                        and a.status = :bookedStatus
                        and a.endsAt > :now)
                  )
              and (:from is null or a.startsAt >= :from)
              and (:to is null or a.startsAt < :to)
            """)
    Page<Appointment> findAllForRepresentative(
            @Param("hospitalIds") Collection<Long> hospitalIds,
            @Param("hospitalId") Long hospitalId,
            @Param("status") AppointmentStatus status,
            @Param("bookedStatus") AppointmentStatus bookedStatus,
            @Param("completedStatus") AppointmentStatus completedStatus,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus,
            @Param("now") Instant now,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    long countByHospitalIdInAndStartsAtGreaterThanEqualAndStartsAtLessThanAndStatusNot(
            Collection<Long> hospitalIds,
            Instant from,
            Instant to,
            AppointmentStatus excludedStatus
    );
}
