package it.cityvoice.api.features.districts.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Municipio {
    I("Centro Storico"),
    II("Parioli e Nomentano"),
    III("Monte Sacro"),
    IV("Tiburtino e Pietralata"),
    V("Prenestino e Centocelle"),
    VI("Le Torri"),
    VII("Appio e Tuscolano"),
    VIII("Ostiense e Garbatella"),
    IX("EUR"),
    X("Ostia e Litorale"),
    XI("Portuense e Magliana"),
    XII("Monteverde"),
    XIII("Aurelio e Boccea"),
    XIV("Monte Mario"),
    XV("Cassia e Flaminio");

    private final String label;

    Municipio(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}