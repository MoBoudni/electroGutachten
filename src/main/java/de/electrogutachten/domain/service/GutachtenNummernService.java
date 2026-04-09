package de.electrogutachten.domain.service;

import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Domain-Service: Erzeugt eindeutige, lesbare Gutachtennummern.
 * Format: EG-{JAHR}-{5-stellige Sequenz}, z.B. "EG-2026-00042"
 *
 * Keine Repository-Abhaengigkeit — reine In-Memory-Sequenz.
 */
@Service
public class GutachtenNummernService {

    private final AtomicInteger sequenz = new AtomicInteger(0);

    public String generiereNaechsteNummer() {
        int jahr = Year.now().getValue();
        int naechste = sequenz.incrementAndGet();
        return "EG-%d-%05d".formatted(jahr, naechste);
    }
}