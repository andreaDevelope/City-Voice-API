package it.cityvoice.api.features.reactions.dto;

import it.cityvoice.api.features.profile.badges.dto.CategoryProgressResponse;

import java.util.List;

public record ReactionResponse(List<CategoryProgressResponse> badgeProgress) {}