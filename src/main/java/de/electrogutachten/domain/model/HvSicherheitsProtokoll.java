package de.electrogutachten.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HvSicherheitsProtokoll: GTÜ/DGuSV-konformes Freischalt-Protokoll.
 *
 * Erweitert um v5-Felder aus DGUV 209-093 (Christiani/Wagner):
 *
 * FA-3.7 — PSA-Schutzklasse der Isolierhandschuhe (EN 60903):
 *   Kl. 0: bis 1.000 V AC (Standard HV-Fahrzeuge)
 *   Kl. 1: bis 7.500 V AC
 *   Kl. 2: bis 17.000 V AC
 *   Kl. 3: bis 26.500 V AC
 *   Kl. 4: bis 36.000 V AC (Spezialfall)
 *
 * FA-3.8 — Wiedereinschaltprotokoll (DGUV Vorschrift 3 §6):
 *   Nach Abschluss der Begutachtung: kontrolliertes Wiedereinschalten
 *   mit Freigabe + Zeitstempel auditierbar dokumentieren.
 */
@Getter
@Builder
public class HvSicherheitsProtokoll {

    // Pflicht-Prüfschritte HV-Freischaltung
    private final boolean psaAngelegt;
    private final boolean hvServicePlugEntfernt;
    private final boolean wartezeit5MinEingehalten;
    private final double  gemesseneSpannungV;
    private final double  isolationswiderstandKOhm;
    private final boolean bereichAbgesperrt;
    private final boolean emvPruefungAbgeschlossen;
    private final List<String> offenePunkte;

    // FA-3.7: PSA-Schutzklasse (EN 60903). -1 = nicht erfasst
    @Builder.Default private final int     handschuhSchutzklasse = -1;
    @Builder.Default private final boolean handschuhGeprueft     = false;

    // FA-3.8: Wiedereinschaltprotokoll (UC-05b, DGUV V3 §6)
    @Builder.Default private final boolean       wiedereinschaltFreigabe   = false;
    private final LocalDateTime wiedereinschaltZeitpunkt;

    /** HV gilt als freigeschaltet wenn alle Pflicht-Kriterien erfüllt. */
    public boolean isHvFreigeschaltet() {
        return psaAngelegt
                && hvServicePlugEntfernt
                && wartezeit5MinEingehalten
                && gemesseneSpannungV < 60.0
                && isolationswiderstandKOhm >= 100.0
                && bereichAbgesperrt;
    }

    /**
     * Vollständigkeit in Prozent über 9 Kriterien
     * (7 Basis + FA-3.7 PSA + FA-3.8 Wiedereinschalten).
     */
    public int getVollstaendigkeitProzent() {
        int erfuellt = 0;
        if (psaAngelegt)                                      erfuellt++;
        if (hvServicePlugEntfernt)                            erfuellt++;
        if (wartezeit5MinEingehalten)                         erfuellt++;
        if (gemesseneSpannungV < 60.0)                        erfuellt++;
        if (isolationswiderstandKOhm >= 100.0)                erfuellt++;
        if (bereichAbgesperrt)                                erfuellt++;
        if (emvPruefungAbgeschlossen)                         erfuellt++;
        if (handschuhSchutzklasse >= 0 && handschuhGeprueft)  erfuellt++; // FA-3.7
        if (wiedereinschaltFreigabe)                          erfuellt++; // FA-3.8
        return (erfuellt * 100) / 9;
    }

    /** PSA-konform nach EN 60903: Klasse gesetzt + Lufttest bestätigt. */
    public boolean isPsaKonform() {
        return handschuhSchutzklasse >= 0 && handschuhGeprueft;
    }

    /** Wiedereinschaltprotokoll vollständig abgeschlossen. */
    public boolean isWiedereinschaltAbgeschlossen() {
        return wiedereinschaltFreigabe && wiedereinschaltZeitpunkt != null;
    }
}
