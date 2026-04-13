package de.electrogutachten.application.usecase;

import de.electrogutachten.application.dto.GutachtenResponse;
import de.electrogutachten.application.mapper.GutachtenMapper;
import de.electrogutachten.domain.model.Gutachten;
import de.electrogutachten.domain.repository.GutachtenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use Case: Gutachten abfragen (Einzel + Archiv-Liste).
 *
 * Zwei Varianten:
 * - findeNachId()       → gibt GutachtenResponse (DTO) zurück — für REST-API
 * - findeGutachtenDomain() → gibt Gutachten (Domain) zurück — für PDF-Generator
 */
@Service
@RequiredArgsConstructor
public class GutachtenAbfragenUseCase {

    private final GutachtenRepository gutachtenRepository;
    private final GutachtenMapper mapper;

    @Transactional(readOnly = true)
    public GutachtenResponse findeNachId(UUID id) {
        return gutachtenRepository.findeNachId(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Gutachten nicht gefunden: " + id));
    }

    /**
     * Gibt das rohe Domain-Aggregat zurück.
     * Wird vom PDF-Generator benötigt, der direkt auf Domain-Objekte zugreift.
     */
    @Transactional(readOnly = true)
    public Gutachten findeGutachtenDomain(UUID id) {
        return gutachtenRepository.findeNachId(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Gutachten nicht gefunden: " + id));
    }

    @Transactional(readOnly = true)
    public List<GutachtenResponse> findeNachTenant(String tenantId) {
        return gutachtenRepository.findeNachTenantId(tenantId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GutachtenResponse> findeNachGutachter(UUID gutachterId) {
        return gutachtenRepository.findeNachGutachterId(gutachterId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}