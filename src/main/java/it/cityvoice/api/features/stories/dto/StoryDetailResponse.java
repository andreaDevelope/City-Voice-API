package it.cityvoice.api.features.stories.dto;

import it.cityvoice.api.features.districts.entity.District;
import it.cityvoice.api.features.stories.entity.Story;
import it.cityvoice.api.features.stories.enums.ReportCategory;
import it.cityvoice.api.features.stories.enums.StoryType;

import java.time.Instant;
import java.util.UUID;

// Come StoryCardResponse, ma con descrizione e testo per intero invece della preview troncata
public record StoryDetailResponse(
        UUID id,
        StoryType type,
        String title,
        String description,
        String storyContent,
        ReportCategory category,
        String district,
        String municipio,
        String municipioLabel,
        Instant createdAt,
        StoryAuthorResponse author,
        long likes,
        long dislikes,
        long comments
) {
    public static StoryDetailResponse from(Story story, StoryAuthorResponse author, long likes, long dislikes, long comments) {
        District district = story.getDistrict();
        return new StoryDetailResponse(
                story.getId(),
                story.getType(),
                story.getTitle(),
                story.getDescription(),
                story.getStoryContent(),
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
}
