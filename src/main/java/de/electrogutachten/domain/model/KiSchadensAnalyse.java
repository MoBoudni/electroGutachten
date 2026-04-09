package de.electrogutachten.domain.model;

import de.electrogutachten.domain.valueobject.SchadensKlassifikation;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * KiSchadensAnalyse: Ergebnis der OpenAI Vision GPT-4 Analyse.
 *
 * Enthält Konfidenz-Scores pro Schadenstyp, erkannte HV-Komponenten
 * und den Hauptschadentyp mit dem höchsten Confidence-Wert.
 */
@Getter
@Builder
public class KiSchadensAnalyse {

    private final SchadensKlassifikation hauptSchadenTyp;
    private final double konfidenzScore;                        // 0.0–1.0
    private final Map<SchadensKlassifikation, Double> alleKonfidenzWerte;
    private final List<String> erkannteHvKomponenten;           // z.B. ["HV-Kabel", "IGBT-Modul"]
    private final String kiBegruendung;                         // Freitext-Erklärung der KI
    private final boolean manuelleValidierungErforderlich;      // wenn Score < 0.85

    /**
     * Prüft, ob manuelle Validierung durch Gutachter nötig ist.
     * Grenzwert: 85 % Konfidenz (Domänenregel).
     */
    public boolean brauchtValidierung() {
        return konfidenzScore < 0.85 || manuelleValidierungErforderlich;
    }

    /**
     * Gibt lesbaren Konfidenzwert als Prozentstring zurück.
     * z.B. "92 %"
     */
    public String getKonfidenzAlsProzent() {
        return "%.0f %%".formatted(konfidenzScore * 100);
    }
}