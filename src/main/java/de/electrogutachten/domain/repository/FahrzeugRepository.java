package de.electrogutachten.domain.repository;

import de.electrogutachten.domain.model.Fahrzeug;
import de.electrogutachten.domain.valueobject.VehicleIdentificationNumber;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FahrzeugRepository {

    Fahrzeug speichern(Fahrzeug fahrzeug);

    Optional<Fahrzeug> findeNachId(UUID id);

    Optional<Fahrzeug> findeNachVin(VehicleIdentificationNumber vin);

    /** Sucht alle bekannten Fahrzeuge eines Herstellers (HV-Katalog). */
    List<Fahrzeug> findeNachHersteller(String hersteller);
}