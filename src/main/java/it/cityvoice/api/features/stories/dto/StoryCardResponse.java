package it.cityvoice.api.features.stories.dto;

import it.cityvoice.api.features.districts.entity.District;
import it.cityvoice.api.features.stories.entity.Story;
import it.cityvoice.api.features.stories.enums.StoryType;

import java.time.Instant;
import java.util.UUID;

public record StoryCardResponse(
        UUID id,
        StoryType type,
        String title,
        String preview,
        String category,
        String district,
        String municipio,
        String municipioLabel,
        Instant createdAt,
        StoryAuthorResponse author,
        long likes,
        long dislikes,
        long comments
) {
    private static final int PREVIEW_MAX_LENGTH = 200;

    public static StoryCardResponse from(Story story, StoryAuthorResponse author, long likes, long dislikes, long comments) {
        District district = story.getDistrict();
        return new StoryCardResponse(
                story.getId(),
                story.getType(),
                story.getTitle(),
                preview(story),
                story.getCategory(),
                district != null ? district.getName() : null,
                district != null ? district.getMunicipio().name() : null,
                district != null ? district.getMunicipio().getLabel() : null,
                story.getCreatedAt(),
                author,
                likes,
                dislikes,
                comments
        );
    }

    private static String preview(Story story) {
        String description = story.getDescription();
        if (description != null && !description.isBlank()) {
            return description;
        }
        String content = story.getStoryContent();
        if (content == null || content.isBlank()) {
            return null;
        }
        return content.length() > PREVIEW_MAX_LENGTH
                ? content.substring(0, PREVIEW_MAX_LENGTH) + "…"
                : content;
    }
}
