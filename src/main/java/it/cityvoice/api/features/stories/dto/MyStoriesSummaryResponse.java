package it.cityvoice.api.features.stories.dto;

public record MyStoriesSummaryResponse(long total, long published, long inReview, long blocked, int municipi) {}
