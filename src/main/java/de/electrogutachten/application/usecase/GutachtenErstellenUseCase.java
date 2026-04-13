package de.electrogutachten.application.usecase;

import de.electrogutachten.application.dto.GutachtenErstellenRequest;
import de.electrogutachten.application.dto.GutachtenResponse;
import de.electrogutachten.domain.model.Fahrzeug;
import de.electrogutachten.domain.model.HvSystem;
import de.electrogutachten.domain.model.Gutachten;
//import de.electrogutachten.domain.model.BatterieTyp;
import de.electrogutachten.domain.repository.GutachtenRepository;
import de.electrogutachten.domain.valueobject.VehicleIdentificationNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GutachtenErstellenUseCase {

    private final GutachtenRepository repository;

    @Transactional
    public GutachtenResponse execute(GutachtenErstellenRequest request) {

        VehicleIdentificationNumber vin = new VehicleIdentificationNumber(request.getVin());

        HvSystem hvSystem = new HvSystem(
                request.getBatterieTyp(),
                request.getNennspannungVolt(),
                request.getNennkapazitaetAh()
        );

        Gutachten gutachten = new Gutachten(request.getTenantId(), vin, hvSystem);

        Gutachten saved = repository.save(gutachten);

        // TODO: Mapper implementieren (später)
        return mapToResponse(saved);
    }

    private GutachtenResponse mapToResponse(Gutachten g) {
        GutachtenResponse r = new GutachtenResponse();
        r.setId(java.util.UUID.fromString(g.getGutachtenNummer())); // vereinfacht
        r.setGutachtenNummer(g.getGutachtenNummer());
        r.setVin(g.getVin().value());
        r.setStatus(g.getStatus().name());
        r.setErstelltAm(g.getErstelltAm());
        r.setHvFreigeschaltet(g.isHvFreigeschaltet());
        return r;
    }
}