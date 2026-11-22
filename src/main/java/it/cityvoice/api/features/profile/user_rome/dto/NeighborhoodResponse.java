package it.cityvoice.api.features.profile.user_rome.dto;

import it.cityvoice.api.features.districts.entity.District;

public record NeighborhoodResponse(
        Long id,
        String name,
        String municipio,
        String municipioLabel
) {
    public static NeighborhoodResponse from(District district) {
        return new NeighborhoodResponse(
                district.getId(),
                district.getName(),
                district.getMunicipio().name(),
                district.getMunicipio().getLabel()
        );
    }
}
