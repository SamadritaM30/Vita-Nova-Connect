package com.medicalaidtracker.representative;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RepresentativeHospitalRepository extends JpaRepository<RepresentativeHospital, UUID> {
    boolean existsByRepresentativeProfileIdAndHospitalId(UUID representativeProfileId, Long hospitalId);

    @Query("""
            select rh.hospital.id
            from RepresentativeHospital rh
            where rh.representativeProfile.id = :profileId
            order by rh.hospital.name
            """)
    List<Long> findHospitalIdsByRepresentativeProfileId(@Param("profileId") UUID profileId);
}
