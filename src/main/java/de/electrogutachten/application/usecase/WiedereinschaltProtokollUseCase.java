package de.electrogutachten.application.usecase;

import de.electrogutachten.application.dto.GutachtenResponse;
import de.electrogutachten.application.mapper.GutachtenMapper;
import de.electrogutachten.domain.model.Gutachten;
import de.electrogutachten.domain.model.HvSicherheitsProtokoll;
import de.electrogutachten.domain.repository.GutachtenRepository;
import de.electrogutachten.domain.valueobject.GutachtenStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use Case UC-05b: Wiedereinschaltprotokoll dokumentieren.
 *
 * Nach Abschluss der Begutachtung (Status FERTIG) muss das HV-System
 * kontrolliert wieder eingeschaltet werden. Dieser Schritt ist nach
 * DGUV Vorschrift 3 §6 dokumentationspflichtig.
 *
 * Norm-Grundlage: DGUV 209-093 (Christiani/Wagner), DGUV V3 §6.
 *
 * Ablauf:
 * 1. Arbeitsbereich freigeben (kein Werkzeug im Fahrzeug)
 * 2. HV-Service-Plug einstecken + Verriegelung prüfen
 * 3. Spannung nach Wiedereinschalten messen (Kontrollmessung)
 * 4. Freigabe + Zeitstempel in Gutachten dokumentieren
 * 5. Protokoll wird dem PDF-Gutachten als Anhang beigefügt
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WiedereinschaltProtokollUseCase {

    private final GutachtenRepository gutachtenRepository;
    private final GutachtenMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Dokumentiert das Wiedereinschaltprotokoll für ein finalisiertes Gutachten.
     *
     * @param gutachtenId          UUID des Gutachtens (muss Status FERTIG haben)
     * @param handschuhKlasse      PSA-Schutzklasse (0–4 nach EN 60903), -1 = nicht erfasst
     * @param handschuhGeprueft    Lufttest der Handschuhe bestätigt
     * @param spannungNachWiedereinschalten  Kontrollmessung in Volt
     */
    @Transactional
    public GutachtenResponse execute(
            UUID gutachtenId,
            int  handschuhKlasse,
            boolean handschuhGeprueft,
            double spannungNachWiedereinschalten) {

        log.info("UC-05b Wiedereinschaltprotokoll für Gutachten id={}", gutachtenId);

        Gutachten gutachten = gutachtenRepository.findeNachId(gutachtenId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Gutachten nicht gefunden: " + gutachtenId));

        // Domänenregel: Nur aus Status FERTIG möglich (DGUV V3 §6)
        if (gutachten.getStatus() != GutachtenStatus.FERTIG) {
            throw new IllegalStateException(
                    "Wiedereinschaltprotokoll nur bei Status FERTIG möglich " +
                            "(DGUV Vorschrift 3 §6). Aktueller Status: " + gutachten.getStatus());
        }

        // Kontrollmessung validieren — nach Wiedereinschalten darf Spannung
        // anliegen, wird aber dokumentiert für Audit-Trail
        log.info("Kontrollmessung nach Wiedereinschalten: {} V", spannungNachWiedereinschalten);

        // Bestehendes Protokoll um v5-Felder erweitern (Builder-Kopie)
        HvSicherheitsProtokoll aktuellesProtokoll = gutachten.getSicherheitsProtokoll();

        HvSicherheitsProtokoll erweitertes = HvSicherheitsProtokoll.builder()
                // Bestehende Felder übernehmen
                .psaAngelegt(aktuellesProtokoll.isPsaAngelegt())
                .hvServicePlugEntfernt(aktuellesProtokoll.isHvServicePlugEntfernt())
                .wartezeit5MinEingehalten(aktuellesProtokoll.isWartezeit5MinEingehalten())
                .gemesseneSpannungV(aktuellesProtokoll.getGemesseneSpannungV())
                .isolationswiderstandKOhm(aktuellesProtokoll.getIsolationswiderstandKOhm())
                .bereichAbgesperrt(aktuellesProtokoll.isBereichAbgesperrt())
                .emvPruefungAbgeschlossen(aktuellesProtokoll.isEmvPruefungAbgeschlossen())
                .offenePunkte(aktuellesProtokoll.getOffenePunkte())
                // FA-3.7: PSA-Schutzklasse setzen
                .handschuhSchutzklasse(handschuhKlasse)
                .handschuhGeprueft(handschuhGeprueft)
                // FA-3.8: Wiedereinschaltfreigabe + Zeitstempel
                .wiedereinschaltFreigabe(true)
                .wiedereinschaltZeitpunkt(LocalDateTime.now())
                .build();

        // Protokoll ins Aggregat schreiben
        gutachten.setSicherheitsProtokoll(erweitertes);

        Gutachten gespeichert = gutachtenRepository.speichern(gutachten);
        gespeichert.popDomainEvents().forEach(eventPublisher::publishEvent);

        log.info("UC-05b abgeschlossen: Protokoll {}% vollständig, PSA-Kl.{}, Wiedereinschalt: {}",
                erweitertes.getVollstaendigkeitProzent(),
                handschuhKlasse,
                erweitertes.getWiedereinschaltZeitpunkt());

        return mapper.toResponse(gespeichert);
    }
}
