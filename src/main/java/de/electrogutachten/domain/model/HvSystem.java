package de.electrogutachten.domain.model;

import de.electrogutachten.domain.valueobject.BatterieTyp;
import de.electrogutachten.domain.valueobject.KuehlungTyp;
import lombok.Builder;
import lombok.Getter;

/**
 * Domain-Objekt: HV-System eines Fahrzeugs.
 * Kapselt alle technischen HV-Parameter (Batterie, Spannung, Kühlung).
 */
@Getter
@Builder
public class HvSystem {

    private final double nennspannung;          // z.B. 201.6 V (Toyota Prius: 168 × 1,2 V)
    private final double kapazitaetAh;          // Nennkapazität in Ah
    private final BatterieTyp batterieTyp;      // LI_ION, NI_MH
    private final KuehlungTyp kuehlungTyp;      // LUFT, FLUESSIGKEIT
    private final int anzahlZellen;
    private final double einzelzellspannung;    // z.B. 1.2 V (NiMH) oder 3.6 V (Li-Ion)

    /**
     * Berechnet den Energieinhalt in kWh.
     * Formel: U × Ah / 1000
     */
    public double getEnergieinhaltKwh() {
        return (nennspannung * kapazitaetAh) / 1000.0;
    }

    /**
     * Prüft, ob Isolationswiderstand den Mindestwert erfüllt.
     * DGUV V3 / IEC 60479: mind. 100 kΩ bei HV-Systemen.
     */
    public boolean istIsolationAusreichend(double isolationswiderstandKOhm) {
        return isolationswiderstandKOhm >= 100.0;
    }

    /**
     * Berechnet Restwert-Faktor basierend auf State-of-Health.
     * SoH ≥ 80 % → Faktor 1.0, lineare Degradation darunter.
     */
    public double berechneRestwertFaktor(double sohProzent) {
        if (sohProzent >= 80.0) return 1.0;
        if (sohProzent >= 60.0) return 0.75;
        if (sohProzent >= 40.0) return 0.45;
        return 0.20;
    }
}