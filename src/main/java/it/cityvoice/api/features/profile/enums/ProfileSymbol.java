package it.cityvoice.api.features.profile.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProfileSymbol {
    MOBILITY,
    ENVIRONMENT,
    URBAN_DECAY,
    SAFETY,
    SERVICES,
    GENERIC;


    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
