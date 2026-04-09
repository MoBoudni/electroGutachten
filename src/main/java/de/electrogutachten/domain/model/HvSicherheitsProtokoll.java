package de.electrogutachten.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HvSicherheitsProtokoll: GTÜ/DGuSV-konformes Freischalt-Protokoll.
 *
 * Dokumentiert alle Sicherheitsschritte vor HV-Arbeiten.
 * Muss vollständig abgeschlossen sein, bevor KI-Analyse startet.
 */
@Getter
@Builder
public class HvSicherheitsProtokoll {

    private final LocalDateTime freischaltZeitpunkt;
    private final boolean psaAngelegt;                  // Isolierhandschuhe Kl. 4, Schutzbrille
    private final boolean hvServicePlugEntfernt;        // orange HV-Service-Plug
    private final boolean wartezeit5MinEingehalten;     // Kondensatoren entladen
    private final double gemesseneSpannungV;            // muss < 25 V AC / < 60 V DC sein
    private final double isolationswiderstandKOhm;      // muss ≥ 100 kΩ sein
    private final boolean bereichAbgesperrt;
    private final boolean emvPruefungAbgeschlossen;
    private final List<String> offenePunkte;            // noch ausstehende Prüfschritte

    /**
     * HV gilt als sicher freigeschaltet, wenn alle Pflichtschritte erfüllt sind.
     * EMV-Prüfung ist wünschenswert, aber kein Freischalt-Blocker.
     */
    public boolean isHvFreigeschaltet() {
        return psaAngelegt
                && hvServicePlugEntfernt
                && wartezeit5MinEingehalten
                && gemesseneSpannungV < 60.0
                && isolationswiderstandKOhm >= 100.0
                && bereichAbgesperrt;
    }

    /**
     * Protokoll-Vollständigkeit in Prozent (für Dashboard-Anzeige).
     */
    public int getVollstaendigkeitProzent() {
        int erfuellt = 0;
        if (psaAngelegt) erfuellt++;
        if (hvServicePlugEntfernt) erfuellt++;
        if (wartezeit5MinEingehalten) erfuellt++;
        if (gemesseneSpannungV < 60.0) erfuellt++;
        if (isolationswiderstandKOhm >= 100.0) erfuellt++;
        if (bereichAbgesperrt) erfuellt++;
        if (emvPruefungAbgeschlossen) erfuellt++;
        return (erfuellt * 100) / 7;
    }
}