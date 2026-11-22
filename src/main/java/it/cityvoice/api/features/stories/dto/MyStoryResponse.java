package it.cityvoice.api.features.stories.dto;

import it.cityvoice.api.features.districts.entity.District;
import it.cityvoice.api.features.stories.entity.Story;
import it.cityvoice.api.features.stories.enums.ReportCategory;
import it.cityvoice.api.features.stories.enums.StoryStatus;
import it.cityvoice.api.features.stories.enums.StoryType;

import java.time.Instant;
import java.util.UUID;

public record MyStoryResponse(
        UUID id,
        StoryType type,
        String title,
        ReportCategory category,
        String district,
        String municipio,
        String municipioLabel,
        StoryStatus status,
        Instant createdAt,
        long likes,
        long dislikes,
        long comments
) {
    public static MyStoryResponse from(Story story, long likes, long dislikes, long comments) {
        District district = story.getDistrict();
        return new MyStoryResponse(
                story.getId(),
                story.getType(),
                story.getTitle(),
                story.getCategory(),
                district != null ? district.getName() : null,
                district != null ? district.getMunicipio().name() : null,
                district != null ? district.getMunicipio().getLabel() : null,
                story.getStatus(),
                story.getCreatedAt(),
                likes,
                dislikes,
                comments
        );
    }
}
