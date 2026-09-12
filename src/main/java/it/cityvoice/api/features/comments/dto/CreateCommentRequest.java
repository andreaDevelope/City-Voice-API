package it.cityvoice.api.features.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCommentRequest(
        @NotNull(message = "il campo Storia non può essere vuoto")
        UUID storyId,
        UUID parentCommentId,
        @NotBlank(message = "il campo Testo non può essere vuoto")
        String content
) {}