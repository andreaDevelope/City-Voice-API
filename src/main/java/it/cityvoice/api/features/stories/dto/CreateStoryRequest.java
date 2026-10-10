package it.cityvoice.api.features.stories.dto;


import it.cityvoice.api.features.stories.enums.StoryType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStoryRequest(
        @NotNull(message = "Tipo obbligatorio")
        StoryType type,
        String category,
        Long districtId,
        @NotBlank(message = "il campo Titolo non può essere vuoto")
        String title,
        String description,
        String storyContent
) {
        @AssertTrue(message = "Categoria e quartiere obbligatori per le segnalazioni, assenti per le storie")
        public boolean isFieldsConsistentWithType() {
                if (type == null) {
                        return true;
                }
                return switch (type) {
                        case REPORT -> category != null && !category.isBlank() && districtId != null;
                        case STORY -> category == null && districtId == null;
                };
        }

        @AssertTrue(message = "Serve almeno una descrizione breve o il testo")
        public boolean isContentPresent() {
                if (type == null) {
                        return true;
                }
                boolean hasDescription = description != null && !description.isBlank();
                boolean hasContent = storyContent != null && !storyContent.isBlank();
                return hasDescription || hasContent;
        }
}