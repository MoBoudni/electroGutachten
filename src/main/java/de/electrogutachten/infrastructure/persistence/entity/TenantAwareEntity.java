package de.electrogutachten.infrastructure.persistence.entity;

import de.electrogutachten.infrastructure.security.TenantContextHolder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Basis-Entity für alle Multi-Tenant-Tabellen.
 *
 * Jede JPA-Entity, die mandantenspezifische Daten enthält,
 * erbt von dieser Klasse. Die tenant_id wird automatisch vor dem
 * ersten Speichern aus dem TenantContextHolder (ThreadLocal) gesetzt.
 *
 * Sicherheitsgarantie: Kein Datensatz kann ohne tenant_id persistiert werden
 * — die Constraint ist auf DB-Ebene (NOT NULL) und JPA-Ebene (@PrePersist)
 * doppelt gesichert.
 *
 * Norm: DSGVO Art. 25 (Datenschutz by Design), ADR-003 (Multi-Tenant).
 */
@Getter
@Setter
@MappedSuperclass
public abstract class TenantAwareEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false, length = 100)
    private String tenantId;

    /**
     * Setzt tenant_id automatisch vor dem ersten INSERT.
     * Verhindert, dass ein Datensatz ohne Mandanten-Zuordnung in die DB gelangt.
     * Wirft IllegalStateException wenn kein Tenant im Context — verhindert
     * versehentliche Cross-Tenant-Schreiboperationen ohne aktive Session.
     */
    @PrePersist
    protected void prePersist() {
        if (this.tenantId == null) {
            String tenantFromContext = TenantContextHolder.get();
            if (tenantFromContext == null || tenantFromContext.isBlank()) {
                throw new IllegalStateException(
                        "Kein Tenant im Context — TenantContextHolder leer. " +
                                "Request korrekt authentifiziert und TenantFilter aktiv?");
            }
            this.tenantId = tenantFromContext;
        }
    }
}
