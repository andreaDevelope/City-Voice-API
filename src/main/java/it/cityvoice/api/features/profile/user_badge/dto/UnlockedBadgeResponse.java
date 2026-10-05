package it.cityvoice.api.features.profile.user_badge.dto;

import it.cityvoice.api.features.profile.badges.entity.Badge;
import it.cityvoice.api.features.profile.user_badge.entity.UserBadge;

import java.time.Instant;

public record UnlockedBadgeResponse(
        Long badgeId,
        String name,
        String description,
        String category,
        int sequenceOrder,
        Instant unlockedAt,
        Integer featuredPosition
) {
    public static UnlockedBadgeResponse from(UserBadge userBadge) {
        Badge badge = userBadge.getBadge();
        return new UnlockedBadgeResponse(
                badge.getId(),
                badge.getName(),
                badge.getDescription(),
                badge.getCategory().getName(),
                badge.getSequenceOrder(),
                userBadge.getUnlockedAt(),
                userBadge.getFeaturedPosition()
        );
    }
}