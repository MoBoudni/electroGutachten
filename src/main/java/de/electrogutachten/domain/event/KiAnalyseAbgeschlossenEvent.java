package de.electrogutachten.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record KiAnalyseAbgeschlossenEvent(
        UUID gutachtenId,
        double konfidenzScore,
        LocalDateTime zeitpunkt
) {
    public KiAnalyseAbgeschlossenEvent(UUID gutachtenId, double konfidenzScore) {
        this(gutachtenId, konfidenzScore, LocalDateTime.now());
    }
}