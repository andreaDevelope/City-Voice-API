package it.cityvoice.api.features.profile.user_rome.services;

import it.cityvoice.api.features.profile.badge.entity.Badge;
import it.cityvoice.api.features.profile.badge.services.BadgeServ;
import it.cityvoice.api.features.profile.category.entity.Category;
import it.cityvoice.api.features.profile.category.services.CategoryServ;
import it.cityvoice.api.features.profile.user_badge.repositories.UserBadgeRepo;
import it.cityvoice.api.features.profile.user_badge.services.UserBadgeService;
import it.cityvoice.api.features.profile.user_rome.dto.UpdateVisualIdentityRequest;
import it.cityvoice.api.features.profile.user_rome.dto.VisualIdentityResponse;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class UserRomeServ {
    private static final String CONTINUITY_CATEGORY = "continuity";
    private static final ZoneId ROME_ZONE = ZoneId.of("Europe/Rome");
    private static final int CONTINUITY_CAP = 7;

    private final UserRomeRepo userRepo;
    private final BadgeServ badgeServ;
    private final CategoryServ categoryServ;
    private final UserBadgeService userBadgeService;
    private final UserBadgeRepo userBadgeRepo;

    @Transactional
    public UserRome findByAppUserId(Long id) {
        UserRome userRome = userRepo.findByAppUserId(id);
        trackDailyAccess(userRome);
        return userRome;
    }

    public UserRome save(UserRome userRome) {
        return userRepo.save(userRome);
    }

    @Transactional
    public VisualIdentityResponse updateVisualIdentity(Long appUserId, @Valid UpdateVisualIdentityRequest request) {
        UserRome userRome = findByAppUserId(appUserId);
        userRome.setSymbol(request.symbol());
        userRome.setColor(request.color());
        UserRome saved = userRepo.save(userRome);
        return new VisualIdentityResponse(saved.getSymbol(), saved.getColor());
    }

    private void trackDailyAccess(UserRome userRome) {
        LocalDate today = LocalDate.now(ROME_ZONE);
        if (today.equals(userRome.getLastActiveDate())) {
            return;
        }
        LocalDate yesterday = today.minusDays(1);
        if (yesterday.equals(userRome.getLastActiveDate())) {
            userRome.setContinuityCounter(Math.min(userRome.getContinuityCounter() + 1, CONTINUITY_CAP));
        } else {
            userRome.setContinuityCounter(0);
        }
        userRome.setLastActiveDate(today);
        userRepo.save(userRome);
        unlockEligibleContinuityBadges(userRome);
    }

    private void unlockEligibleContinuityBadges(UserRome userRome) {
        Category category = categoryServ.getCategoryByName(CONTINUITY_CATEGORY);
        List<Badge> badges = badgeServ.getAllBadgesForCategory(category.getId());
        for (Badge badge : badges) {
            boolean thresholdReached = badge.getMissionThreshold() > 0
                    && badge.getMissionThreshold() <= userRome.getContinuityCounter();
            if (thresholdReached && !userBadgeRepo.existsByUserRomeAndBadge(userRome, badge)) {
                userBadgeService.unlock(userRome, badge);
            }
        }
    }
}