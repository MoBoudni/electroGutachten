package de.electrogutachten.infrastructure.persistence.entity;

import de.electrogutachten.domain.valueobject.GutachtenStatus;
import de.electrogutachten.domain.valueobject.SchadensKlassifikation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA-Entity: Gutachten-Persistenz.
 *
 * Erbt tenant_id automatisch von TenantAwareEntity (@MappedSuperclass).
 * Erweitert um v5-Felder:
 * - handschuhSchutzklasse / handschuhGeprueft  (FA-3.7, EN 60903)
 * - wiedereinschaltFreigabe / zeitpunkt        (FA-3.8, UC-05b, DGUV V3 §6)
 */
@Entity
@Table(name = "gutachten", indexes = {
        @Index(name = "idx_gutachten_nummer",   columnList = "gutachten_nummer", unique = true),
        @Index(name = "idx_gutachten_tenant",   columnList = "tenant_id"),
        @Index(name = "idx_gutachten_status",   columnList = "status"),
        @Index(name = "idx_gutachten_erstellt", columnList = "erstellt_am")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GutachtenEntity extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "gutachten_nummer", nullable = false, unique = true, length = 20)
    private String gutachtenNummer;

    @Column(name = "gutachter_id", nullable = false)
    private UUID gutachterId;

    // Fahrzeug
    @Column(name = "vin",        nullable = false, length = 17)  private String vin;
    @Column(name = "hersteller", nullable = false)               private String hersteller;
    @Column(name = "modell",     nullable = false)               private String modell;
    @Column(name = "baujahr",    nullable = false)               private int    baujahr;
    @Column(name = "fahrzeug_typ", nullable = false)             private String fahrzeugTyp;

    // HV-System
    @Column(name = "nennspannung_v")   private Double nennspannungV;
    @Column(name = "kapazitaet_ah")    private Double kapazitaetAh;
    @Column(name = "batterie_typ")     private String batterieTyp;

    // Status & Kalkulation
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GutachtenStatus status;

    @Column(name = "reparaturkosten_eur", precision = 10, scale = 2)
    private BigDecimal reparaturkostenEur;

    // KI-Analyse
    @Enumerated(EnumType.STRING)
    @Column(name = "haupt_schaden_typ")     private SchadensKlassifikation hauptSchadenTyp;
    @Column(name = "ki_konfidenz_score")    private Double  kiKonfidenzScore;
    @Column(name = "ki_begruendung", columnDefinition = "TEXT") private String kiBegruendung;
    @Column(name = "validierung_erforderlich") private Boolean validierungErforderlich;

    // Batterie-Analyse
    @Column(name = "soh_prozent")               private Double     sohProzent;
    @Column(name = "soc_prozent")               private Double     socProzent;
    @Column(name = "isolationswiderstand_kohm") private Double     isolationswiderstandKOhm;
    @Column(name = "restwert_eur", precision = 10, scale = 2) private BigDecimal restwertEur;

    // Sicherheitsprotokoll Basis
    @Column(name = "hv_freigeschaltet")           private Boolean hvFreigeschaltet;
    @Column(name = "protokoll_vollstaendigkeit_pct") private Integer protokollVollstaendigkeitPct;
    @Column(name = "gemessene_spannung_v")         private Double  gemesseneSpannungV;

    // FA-3.7: PSA-Schutzklasse nach EN 60903
    // Kl. 0 = bis 1.000 V AC (Standard HV-Fahrzeuge). -1 = nicht erfasst.
    @Column(name = "handschuh_schutzklasse")
    @Builder.Default
    private int handschuhSchutzklasse = -1;

    // Lufttest-Bestätigung vor jedem Einsatz (DGUV 209-093)
    @Column(name = "handschuh_geprueft")
    private Boolean handschuhGeprueft;

    // FA-3.8 / UC-05b: Wiedereinschaltprotokoll (DGUV Vorschrift 3 §6)
    @Column(name = "wiedereinschalt_freigabe")
    private Boolean wiedereinschaltFreigabe;

    @Column(name = "wiedereinschalt_zeitpunkt")
    private LocalDateTime wiedereinschaltZeitpunkt;

    // Audit
    @CreationTimestamp
    @Column(name = "erstellt_am", updatable = false)
    private LocalDateTime erstelltAm;

    @UpdateTimestamp
    @Column(name = "geaendert_am")
    private LocalDateTime geaendertAm;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GutachtenEntity e)) return false;
        return id != null && id.equals(e.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
