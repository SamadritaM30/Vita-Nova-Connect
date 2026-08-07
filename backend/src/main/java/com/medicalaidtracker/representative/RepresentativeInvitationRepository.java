package com.medicalaidtracker.representative;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RepresentativeInvitationRepository extends JpaRepository<RepresentativeInvitation, UUID> {
    List<RepresentativeInvitation> findAllByEmailIgnoreCaseAndActiveTrue(String email);
}
