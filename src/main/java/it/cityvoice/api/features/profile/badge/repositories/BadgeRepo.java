package it.cityvoice.api.features.profile.badge.repositories;

import it.cityvoice.api.features.profile.badge.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeRepo extends JpaRepository<Badge, Long> {

    List<Badge> findByCategoryIdOrderBySequenceOrderAsc(Long categoryId);
}
