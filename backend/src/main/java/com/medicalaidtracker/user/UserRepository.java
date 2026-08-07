package com.medicalaidtracker.user;

import com.medicalaidtracker.common.domain.DomainEnums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);
}
