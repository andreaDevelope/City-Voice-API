package it.cityvoice.api.features.stories.dto;

import it.cityvoice.api.features.profile.badges.dto.CategoryProgressResponse;
import it.cityvoice.api.features.stories.entity.Story;
import it.cityvoice.api.features.stories.enums.StoryStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StoryResponse(
        UUID id,
        String category,
        String district,
        String title,
        String description,
        String storyContent,
        StoryStatus status,
        Instant createdAt,
        List<CategoryProgressResponse> badgeProgress
) {
    public static StoryResponse from(Story story, List<CategoryProgressResponse> badgeProgress) {
        return new StoryResponse(
                story.getId(),
                story.getCategory(),
                story.getDistrict(),
                story.getTitle(),
                story.getDescription(),
                story.getStoryContent(),
                story.getStatus(),
                story.getCreatedAt(),
                badgeProgress
        );
    }
}