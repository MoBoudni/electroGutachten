package de.electrogutachten.domain.valueobject;

import java.util.regex.Pattern;

/**
 * Value Object: Fahrzeugidentifikationsnummer (VIN / FIN).
 * Immutabel, validiert nach ISO 3779 (17 Zeichen, kein I/O/Q).
 */
public record VehicleIdentificationNumber(String value) {

    private static final Pattern VIN_PATTERN =
            Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    public VehicleIdentificationNumber {
        if (value == null || !VIN_PATTERN.matcher(value.toUpperCase()).matches()) {
            throw new IllegalArgumentException(
                    "Ungültige VIN: '%s'. Erwartet 17 Zeichen (ohne I, O, Q).".formatted(value));
        }
        value = value.toUpperCase();
    }

    /** Gibt Weltherstellerkennung (WMI) zurück — erste 3 Zeichen. */
    public String getWmi() {
        return value.substring(0, 3);
    }

    /** Gibt Fahrzeugbeschreibungsabschnitt (VDS) zurück — Zeichen 4–9. */
    public String getVds() {
        return value.substring(3, 9);
    }

    @Override
    public String toString() {
        return value;
    }
}