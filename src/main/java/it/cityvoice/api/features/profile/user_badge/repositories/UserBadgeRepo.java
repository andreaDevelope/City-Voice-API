package it.cityvoice.api.features.profile.user_badge.repositories;
import it.cityvoice.api.features.profile.user_badge.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeRepo extends JpaRepository<UserBadge,Long> {
}
