package de.electrogutachten.domain.valueobject;

/**
 * Fahrzeugtyp nach Antriebskonzept.
 * Bestimmt HV-Systemtyp und Prüfanforderungen.
 */
public enum FahrzeugTyp {
    FULL_HYBRID("Full-Hybrid"),          // z.B. Toyota Prius
    MILD_HYBRID("Mild-Hybrid"),          // z.B. Mercedes S 400 Hybrid
    BEV("Elektrofahrzeug"),              // z.B. Smart fortwo ED
    FCEV("Brennstoffzelle"),             // z.B. Mercedes B-Klasse F-Cell
    PHEV("Plug-in-Hybrid");

    private final String bezeichnung;

    FahrzeugTyp(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }
}