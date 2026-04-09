package de.electrogutachten.domain.model;

import de.electrogutachten.domain.valueobject.FahrzeugTyp;
import de.electrogutachten.domain.valueobject.VehicleIdentificationNumber;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Domain-Aggregate: Fahrzeug
 * Repräsentiert ein Hybrid- oder Elektrofahrzeug mit HV-System.
 */
@Getter
@Builder
public class Fahrzeug {

    private final UUID id;
    private final VehicleIdentificationNumber vin;
    private final String hersteller;
    private final String modell;
    private final int baujahr;
    private final FahrzeugTyp typ;             // FULL_HYBRID, MILD_HYBRID, BEV, FCEV
    private final HvSystem hvSystem;

    /** Prüft, ob das Fahrzeug ein HV-System über 60 V DC hat (DGUV-Grenzwert). */
    public boolean isHochvoltFahrzeug() {
        return hvSystem.getNennspannung() > 60.0;
    }

    /** Gibt Displayname zurück, z.B. "Toyota Prius 2021 (Full-Hybrid)". */
    public String getAnzeigename() {
        return "%s %s %d (%s)".formatted(hersteller, modell, baujahr, typ.getBezeichnung());
    }
}