package de.electrogutachten.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * DTO: Anfrage für UC-05b — Wiedereinschaltprotokoll.
 * Dokumentiert die PSA-Schutzklasse und die Wiedereinschalt-Freigabe
 * nach DGUV Vorschrift 3 §6 und DGUV 209-093.
 */
@Getter
@Builder
public class WiedereinschaltRequest {

    private final UUID gutachtenId;

    /**
     * PSA-Schutzklasse der Isolierhandschuhe (EN 60903).
     * 0 = bis 1.000 V AC — Standard für HV-Fahrzeuge.
     * -1 = nicht erfasst (führt zu unvollständigem Protokoll).
     */
    @Min(value = -1, message = "Schutzklasse muss zwischen -1 und 4 liegen")
    @Max(value =  4, message = "Schutzklasse muss zwischen -1 und 4 liegen")
    private final int handschuhSchutzklasse;

    /** Lufttest der Handschuhe bestätigt (DGUV 209-093). */
    private final boolean handschuhGeprueft;

    /**
     * Kontrollmessung nach Wiedereinschalten in Volt.
     * Wird für den Audit-Trail dokumentiert.
     */
    @DecimalMin(value = "0.0", message = "Spannung muss positiv sein")
    private final double spannungNachWiedereinschaltenV;
}
