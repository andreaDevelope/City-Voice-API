package it.cityvoice.api.features.stories.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStoryRequest(
        @NotBlank(message = "il campo Categoria non può essere vuoto")
        String category,
        @NotNull(message = "il campo Quartiere non può essere vuoto")
        Long districtId,
        @NotBlank(message = "il campo Titolo non può essere vuoto")
        String title,
        @NotBlank(message = "il campo Descrizione non può essere vuoto")
        String description,
        @NotBlank(message = "il campo Testo non può essere vuoto")
        String storyContent
) {}