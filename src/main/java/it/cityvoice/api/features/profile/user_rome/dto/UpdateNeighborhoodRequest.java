package it.cityvoice.api.features.profile.user_rome.dto;

// null districtId clears the neighborhood, so it carries no @NotNull
public record UpdateNeighborhoodRequest(Long districtId) {}
