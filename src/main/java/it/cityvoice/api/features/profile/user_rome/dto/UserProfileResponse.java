package it.cityvoice.api.features.profile.user_rome.dto;

import it.cityvoice.api.features.profile.enums.ProfileColor;
import it.cityvoice.api.features.profile.enums.ProfileSymbol;

public record UserProfileResponse(
        String username,
        ProfileSymbol symbol,
        ProfileColor color,
        String neighborhood
) {}
