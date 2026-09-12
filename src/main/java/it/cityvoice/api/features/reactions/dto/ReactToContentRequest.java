package it.cityvoice.api.features.reactions.dto;

import it.cityvoice.api.features.reactions.enums.ReactionType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReactToContentRequest(
        UUID storyId,
        UUID commentId,
        @NotNull(message = "il campo Tipo Reazione non può essere vuoto")
        ReactionType type
) {}