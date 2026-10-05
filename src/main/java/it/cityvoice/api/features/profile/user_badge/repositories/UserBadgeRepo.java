package it.cityvoice.api.features.profile.user_badge.repositories;

import it.cityvoice.api.features.profile.badges.entity.Badge;
import it.cityvoice.api.features.profile.user_badge.entity.UserBadge;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface UserBadgeRepo extends JpaRepository<UserBadge, Long> {

     boolean existsByUserRomeAndBadge(UserRome userRome, Badge badge);

     // badge sbloccati dall'utente, dal più vecchio al più recente
     List<UserBadge> findByUserRomeOrderByUnlockedAtAsc(UserRome userRome);

     // badge dell'utente tra quelli scelti dal FE (id dei Badge, non degli UserBadge)
     List<UserBadge> findByUserRomeAndBadgeIdIn(UserRome userRome, Collection<Long> badgeIds);

     // azzera le posizioni prima di assegnare le nuove; svuota la cache perché la bulk update non aggiorna le entità in memoria
     @Modifying(flushAutomatically = true, clearAutomatically = true)
     @Query("UPDATE UserBadge ub SET ub.featuredPosition = null WHERE ub.userRome = :userRome")
     void clearFeaturedPositions(@Param("userRome") UserRome userRome);
}