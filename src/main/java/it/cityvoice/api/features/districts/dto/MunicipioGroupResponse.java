package it.cityvoice.api.features.districts.dto;

import it.cityvoice.api.features.districts.enums.Municipio;

import java.util.List;

public record MunicipioGroupResponse(
        Municipio municipio,
        String label,
        List<DistrictResponse> districts
) {}