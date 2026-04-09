package de.electrogutachten.domain.repository;

import de.electrogutachten.domain.model.Gutachten;
import de.electrogutachten.domain.valueobject.GutachtenStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository-Interface für Gutachten (Domain-Port).
 * Implementierung liegt in infrastructure.persistence.
 * Abhängigkeit zeigt immer von Infrastructure → Domain (Clean Architecture).
 */
public interface GutachtenRepository {

    Gutachten speichern(Gutachten gutachten);

    Optional<Gutachten> findeNachId(UUID id);

    Optional<Gutachten> findeNachNummer(String gutachtenNummer);

    List<Gutachten> findeNachTenantId(String tenantId);

    List<Gutachten> findeNachStatus(GutachtenStatus status);

    /** Findet alle Gutachten eines Gutachters (für Archiv-Dashboard). */
    List<Gutachten> findeNachGutachterId(UUID gutachterId);

    void loeschen(UUID id);
}