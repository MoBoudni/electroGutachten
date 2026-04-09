package de.electrogutachten.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record GutachtenErstelltEvent(
        UUID gutachtenId,
        String gutachtenNummer,
        String fahrzeugAnzeigename,
        LocalDateTime zeitpunkt
) {
    // Canonical constructor (alle 4 Felder) — explizit deklariert
    public GutachtenErstelltEvent(
            UUID gutachtenId,
            String gutachtenNummer,
            String fahrzeugAnzeigename,
            LocalDateTime zeitpunkt) {
        this.gutachtenId = gutachtenId;
        this.gutachtenNummer = gutachtenNummer;
        this.fahrzeugAnzeigename = fahrzeugAnzeigename;
        this.zeitpunkt = zeitpunkt;
    }

    // Convenience-Constructor ohne Zeitpunkt
    public GutachtenErstelltEvent(UUID gutachtenId, String gutachtenNummer, String fahrzeugAnzeigename) {
        this(gutachtenId, gutachtenNummer, fahrzeugAnzeigename, LocalDateTime.now());
    }
}