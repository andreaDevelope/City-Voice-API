package it.cityvoice.api.features.stories.dto;

import it.cityvoice.api.features.profile.user_badge.entity.UserBadge;

public record FeaturedBadgeResponse(
        Long id,
        String name,
        String category,
        int sequenceOrder
) {
    public static FeaturedBadgeResponse from(UserBadge userBadge) {
        return new FeaturedBadgeResponse(
                userBadge.getBadge().getId(),
                userBadge.getBadge().getName(),
                userBadge.getBadge().getCategory().getName(),
                userBadge.getBadge().getSequenceOrder()
        );
    }
}
