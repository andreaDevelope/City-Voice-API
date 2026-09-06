package it.cityvoice.api.features.profile.badge.dto;

public record BadgeDto(
        Long id,
        String name,
        String description,
        int missionThreshold,
        int sequenceOrder
) {}
