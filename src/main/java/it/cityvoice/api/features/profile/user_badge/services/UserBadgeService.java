package it.cityvoice.api.features.profile.user_badge.services;

import it.cityvoice.api.features.profile.badges.entity.Badge;
import it.cityvoice.api.features.profile.user_badge.entity.UserBadge;
import it.cityvoice.api.features.profile.user_badge.repositories.UserBadgeRepo;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBadgeService {

    private final UserBadgeRepo userBadgeRepo;

    public List<UserBadge> getAllBadges() {
        return userBadgeRepo.findAll();
    }

    public UserBadge getBadgeById(Long id) {
        return userBadgeRepo.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    public UserBadge unlock(UserRome userRome, Badge badge) {
        UserBadge userBadge = new UserBadge();
        userBadge.setUserRome(userRome);
        userBadge.setBadge(badge);
        userBadge.setUnlockedAt(Instant.now());
        userBadge.setFeatured(false);
        return userBadgeRepo.save(userBadge);
    }

}
