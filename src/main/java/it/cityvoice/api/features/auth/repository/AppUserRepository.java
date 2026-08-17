package it.cityvoice.api.features.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import it.cityvoice.api.features.auth.entity.AppUser;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
