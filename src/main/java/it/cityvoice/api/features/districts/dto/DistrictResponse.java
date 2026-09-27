package it.cityvoice.api.features.districts.dto;

import it.cityvoice.api.features.districts.entity.District;
import it.cityvoice.api.features.districts.enums.DistrictType;

public record DistrictResponse(
        Long id,
        String name,
        DistrictType type
) {
    public static DistrictResponse from(District district) {
        return new DistrictResponse(district.getId(), district.getName(), district.getType());
    }
}