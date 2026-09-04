package it.cityvoice.api.features.profile.user_rome.dto;


import it.cityvoice.api.features.profile.enums.ProfileColor;
import it.cityvoice.api.features.profile.enums.ProfileSymbol;
import jakarta.validation.constraints.NotNull;

public record UpdateVisualIdentityRequest(
        @NotNull(message = "Il simbolo è obbligatorio")
        ProfileSymbol symbol,

        @NotNull(message = "Il colore è obbligatorio")
        ProfileColor color
) {}
