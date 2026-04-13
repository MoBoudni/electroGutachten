package de.electrogutachten.application.usecase;

import de.electrogutachten.application.dto.GutachtenResponse;
import de.electrogutachten.application.dto.KiAnalyseRequest;
import de.electrogutachten.domain.model.gutachten.Gutachten;
import de.electrogutachten.domain.model.gutachten.KiSchadensAnalyse;
import de.electrogutachten.domain.repository.GutachtenRepository;
import de.electrogutachten.infrastructure.ai.HvSchadensAnalyseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KiAnalyseUseCase {

    private final GutachtenRepository repository;
    private final HvSchadensAnalyseService aiService;

    @Transactional
    public GutachtenResponse execute(UUID id, KiAnalyseRequest request) {

        Gutachten gutachten = repository.findById(id.toString())
                .orElseThrow(() -> new IllegalArgumentException("Gutachten nicht gefunden"));

        gutachten.starteKiAnalyse();

        // KI-Analyse ausführen
        KiSchadensAnalyse analyse = aiService.analysiereBilder(request.getBilderBase64(), gutachten.getHvSystem());

        gutachten.schliesseKiAnalyseAb(analyse);

        Gutachten saved = repository.save(gutachten);

        // TODO: Mapper
        return mapToResponse(saved);
    }

    private GutachtenResponse mapToResponse(Gutachten g) {
        GutachtenResponse r = new GutachtenResponse();
        r.setGutachtenNummer(g.getGutachtenNummer());
        r.setStatus(g.getStatus().name());
        r.setReparaturkostenEur(g.getReparaturkostenEur());
        r.setSohProzent(g.getHvSystem().getSoHProzent());
        if (g.getKiAnalyse() != null) {
            r.setHauptSchadenTyp(g.getKiAnalyse().getHauptSchadenTyp().name());
            r.setKonfidenzScore(g.getKiAnalyse().getHauptKonfidenzScore());
        }
        return r;
    }
}