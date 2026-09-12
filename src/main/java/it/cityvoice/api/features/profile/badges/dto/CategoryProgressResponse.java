package it.cityvoice.api.features.profile.badges.dto;

public record CategoryProgressResponse(
        String category,
        BadgeDto currentBadge,
        BadgeDto nextBadge,
        int counter
) {}
