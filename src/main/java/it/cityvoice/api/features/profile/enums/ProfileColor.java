package it.cityvoice.api.features.profile.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ProfileColor {
    NEUTRAL,
    COMPLAINT,
    WARNING,
    IMPACT,
    DISCRETION;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
