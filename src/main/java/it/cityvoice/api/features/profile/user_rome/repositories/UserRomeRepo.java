package it.cityvoice.api.features.profile.user_rome.repositories;

import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.shared.exceptions.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRomeRepo extends JpaRepository<UserRome, Long> {
    Optional<UserRome> findOptionalByAppUserId(Long appUserId);
    default UserRome findByAppUserId(Long appUserId) {
        return findOptionalByAppUserId(appUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserRome non trovato per appUserId: " + appUserId));
    }
}
