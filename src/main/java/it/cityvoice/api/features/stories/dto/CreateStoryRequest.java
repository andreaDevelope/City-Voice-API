package it.cityvoice.api.features.stories.dto;


import jakarta.validation.constraints.NotBlank;

public record CreateStoryRequest(
        @NotBlank(message = "il campo Categoria non può essere vuoto")
        String category,
        @NotBlank(message = "il campo Quartiere non può essere vuoto")
        String district,
        @NotBlank(message = "il campo Titolo non può essere vuoto")
        String title,
        @NotBlank(message = "il campo Descrizione non può essere vuoto")
        String description,
        @NotBlank(message = "il campo Testo non può essere vuoto")
        String storyContent
) {}