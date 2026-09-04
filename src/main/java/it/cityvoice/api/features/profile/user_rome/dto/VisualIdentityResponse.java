package it.cityvoice.api.features.profile.user_rome.dto;

import it.cityvoice.api.features.profile.enums.ProfileColor;
import it.cityvoice.api.features.profile.enums.ProfileSymbol;

public record VisualIdentityResponse(
        ProfileSymbol symbol,
        ProfileColor color
) {}
