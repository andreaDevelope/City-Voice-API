package it.cityvoice.api.features.profile.user_badge.repositories;
import it.cityvoice.api.features.profile.badge.entity.Badge;
import it.cityvoice.api.features.profile.user_badge.entity.UserBadge;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeRepo extends JpaRepository<UserBadge,Long> {
     boolean existsByUserRomeAndBadge(UserRome userRome, Badge badge);
}
