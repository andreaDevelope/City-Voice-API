package it.cityvoice.api.features.profile.user_badge.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FeaturedBadgesRequest(
        @NotNull(message = "La lista dei badge è obbligatoria")
        @Size(max = 3, message = "Puoi mettere in evidenza al massimo 3 badge")
        List<@NotNull(message = "Il badge non può essere vuoto") Long> badgeIds
) {}