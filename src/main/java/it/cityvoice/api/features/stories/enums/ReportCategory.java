package it.cityvoice.api.features.stories.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

//Stored as text in the DB: after the first deploy, values can only be added, never renamed or removed.
public enum ReportCategory {
    STRADE,
    ACQUA_FOGNE,
    RIFIUTI,
    VERDE,
    TRASPORTO_PUBBLICO,
    ACCESSIBILITA,
    SICUREZZA,
    BUROCRAZIA,
    MULTE,
    CONTROLLI_STRADALI,
    ALTRO;

    @JsonValue
    public String toJson() {
        return this.name().toLowerCase();
    }

    public static Optional<ReportCategory> fromJson(String value){
        return Arrays.stream(values()).filter(category -> category.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
