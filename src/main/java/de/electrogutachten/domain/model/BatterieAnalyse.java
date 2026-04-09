package de.electrogutachten.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * BatterieAnalyse: Ergebnis der HV-Batterie-Diagnose.
 *
 * Enthält SoH, SoC, Isolationswiderstand und berechneten Restwert.
 * Basiert auf den NiMH/Li-Ion-Parametern aus dem HV-Fachkraft-Lehrbuch.
 */
@Getter
@Builder
public class BatterieAnalyse {

    private final double sohProzent;               // State of Health 0–100 %
    private final double socProzent;               // State of Charge 0–100 %
    private final double isolationswiderstandKOhm; // Isolationswiderstand in kΩ
    private final double kapazitaetAktuellAh;      // gemessene Kapazität
    private final double kapazitaetNominalAh;      // Nennkapazität aus Typenschild
    private final String kuehlungsStatus;          // "OK", "EINGESCHRAENKT", "DEFEKT"
    private final double restwertEur;              // berechneter Restwert

    /**
     * Bewertet Batterie nach DIN EN 62133 / HV-Fachkraft-Richtwerten.
     */
    public BatterieBewertung getBewertung() {
        if (sohProzent >= 80) return BatterieBewertung.GUT;
        if (sohProzent >= 60) return BatterieBewertung.MAESSIG;
        if (sohProzent >= 40) return BatterieBewertung.SCHLECHT;
        return BatterieBewertung.TAUSCH_ERFORDERLICH;
    }

    public boolean isIsolationKritisch() {
        return isolationswiderstandKOhm < 100.0; // DGUV-Grenzwert
    }

    public enum BatterieBewertung {
        GUT, MAESSIG, SCHLECHT, TAUSCH_ERFORDERLICH
    }
}