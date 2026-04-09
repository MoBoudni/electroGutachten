package de.electrogutachten.domain.model;

import de.electrogutachten.domain.event.GutachtenErstelltEvent;
import de.electrogutachten.domain.event.KiAnalyseAbgeschlossenEvent;
import de.electrogutachten.domain.valueobject.GutachtenStatus;
import de.electrogutachten.domain.valueobject.SchadensKlassifikation;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Domain-Aggregate-Root: Gutachten
 *
 * Zentrales Objekt der electroGutachten-Domäne.
 * Kapselt den vollständigen Lebenszyklus eines HV-Gutachtens:
 * Erstellung → KI-Analyse → Validierung → PDF-Generierung.
 *
 * Domain-Events werden intern gesammelt und vom Application-Layer
 * nach dem Speichern publiziert.
 */
@Getter
@Builder
public class Gutachten {

    private final UUID id;
    private final String gutachtenNummer;       // z.B. "EG-2026-00042"
    private final Fahrzeug fahrzeug;
    private final Gutachter gutachter;
    private final LocalDateTime erstelltAm;
    private GutachtenStatus status;

    // --- HV-Befunde ---
    private BatterieAnalyse batterieAnalyse;
    private KiSchadensAnalyse kiSchadensAnalyse;
    private HvSicherheitsProtokoll sicherheitsProtokoll;

    // --- Kalkulation ---
    private BigDecimal reparaturkostenEur;

    // --- Domain-Events ---
    @Builder.Default
    private final List<Object> domainEvents = new ArrayList<>();

    // -----------------------------------------------------------------------
    // Business-Methoden
    // -----------------------------------------------------------------------

    /**
     * Startet die KI-Analyse. Setzt Status auf IN_ANALYSE.
     * Wirft Exception, wenn kein Sicherheitsprotokoll vorliegt.
     */
    public void starteKiAnalyse() {
        if (sicherheitsProtokoll == null || !sicherheitsProtokoll.isHvFreigeschaltet()) {
            throw new IllegalStateException(
                    "HV-Freischaltung muss vor KI-Analyse abgeschlossen sein.");
        }
        this.status = GutachtenStatus.IN_ANALYSE;
    }

    /**
     * Schließt KI-Analyse ab und speichert Ergebnis.
     * Berechnet Reparaturkosten automatisch aus Schadensklassifikation.
     */
    public void schliesseKiAnalyseAb(KiSchadensAnalyse analyse) {
        this.kiSchadensAnalyse = analyse;
        this.reparaturkostenEur = berechneReparaturkosten(analyse.getHauptSchadenTyp());
        this.status = GutachtenStatus.ANALYSIERT;
        domainEvents.add(new KiAnalyseAbgeschlossenEvent(id, analyse.getKonfidenzScore()));
    }

    /**
     * Finalisiert das Gutachten (bereit für PDF-Export).
     */
    public void finalisiere() {
        if (status != GutachtenStatus.ANALYSIERT) {
            throw new IllegalStateException("Gutachten muss analysiert sein vor Finalisierung.");
        }
        this.status = GutachtenStatus.FERTIG;
        domainEvents.add(new GutachtenErstelltEvent(id, gutachtenNummer, fahrzeug.getAnzeigename()));
    }

    /** Löscht Domain-Events nach Publikation. */
    public List<Object> popDomainEvents() {
        List<Object> events = Collections.unmodifiableList(new ArrayList<>(domainEvents));
        domainEvents.clear();
        return events;
    }

    // -----------------------------------------------------------------------
    // Private Hilfsmethoden
    // -----------------------------------------------------------------------

    private BigDecimal berechneReparaturkosten(SchadensKlassifikation schadensTyp) {
        return switch (schadensTyp) {
            case LICHTBOGEN         -> new BigDecimal("2450.00");
            case ISOLATIONSBRUCH    -> new BigDecimal("1800.00");
            case IGBT_VERSAGEN      -> new BigDecimal("3200.00");
            case MECH_BESCHAEDIGUNG -> new BigDecimal("850.00");
            case KEIN_SCHADEN       -> BigDecimal.ZERO;
        };
    }
}