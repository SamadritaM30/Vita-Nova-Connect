package com.medicalaidtracker.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RepresentativeProfileRepository extends JpaRepository<RepresentativeProfile, UUID> {
    Optional<RepresentativeProfile> findByUserId(UUID userId);
}
