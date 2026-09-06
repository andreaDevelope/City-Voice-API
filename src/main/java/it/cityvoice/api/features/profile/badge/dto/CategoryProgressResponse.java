package it.cityvoice.api.features.profile.badge.dto;

public record CategoryProgressResponse(
        String category,
        BadgeDto currentBadge,
        BadgeDto nextBadge,
        int counter
) {}
