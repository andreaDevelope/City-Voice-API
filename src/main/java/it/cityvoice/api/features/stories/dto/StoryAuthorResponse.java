package it.cityvoice.api.features.stories.dto;

import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.enums.ProfileColor;
import it.cityvoice.api.features.profile.user_rome.enums.ProfileSymbol;

import java.util.List;

public record StoryAuthorResponse(
        String username,
        ProfileSymbol symbol,
        ProfileColor color,
        List<FeaturedBadgeResponse> featuredBadges
) {
    public static StoryAuthorResponse from(UserRome userRome, List<FeaturedBadgeResponse> featuredBadges) {
        return new StoryAuthorResponse(
                userRome.getAppUser().getUsername(),
                userRome.getSymbol(),
                userRome.getColor(),
                featuredBadges
        );
    }
}
