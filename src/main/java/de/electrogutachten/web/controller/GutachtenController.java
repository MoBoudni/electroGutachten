package de.electrogutachten.web.controller;

import de.electrogutachten.application.dto.*;
import de.electrogutachten.application.usecase.*;
import de.electrogutachten.infrastructure.pdf.GutachtenPdfGenerator;
import de.electrogutachten.infrastructure.security.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/gutachten")
@RequiredArgsConstructor
@Tag(name = "Gutachten", description = "HV-Gutachten erstellen, analysieren und dokumentieren")
public class GutachtenController {

    private final GutachtenErstellenUseCase erstellenUseCase;
    private final KiAnalyseUseCase kiAnalyseUseCase;
    private final GutachtenAbfragenUseCase abfragenUseCase;
    private final WiedereinschaltProtokollUseCase wiedereinschaltUseCase;
    private final GutachtenPdfGenerator pdfGenerator;

    // UC-01: Neues Gutachten
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('GUTACHTER') or hasRole('ADMIN')")
    @Operation(summary = "Neues HV-Gutachten erstellen (UC-01)")
    public ResponseEntity<GutachtenResponse> erstellen(
            @Valid @RequestBody GutachtenErstellenRequest request) {

        log.info("POST /api/gutachten VIN={}", request.getVin());
        GutachtenResponse response = erstellenUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // UC-03: KI-Analyse
    @PostMapping("/{id}/analyse")
    @PreAuthorize("hasRole('GUTACHTER') or hasRole('ADMIN')")
    @Operation(summary = "KI-Bildanalyse starten (UC-03)")
    public ResponseEntity<GutachtenResponse> starteAnalyse(
            @PathVariable UUID id,
            @Valid @RequestBody KiAnalyseRequest request) {

        log.info("POST /api/gutachten/{}/analyse ({} Bilder)", id, request.getBilderBase64().size());
        GutachtenResponse response = kiAnalyseUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    // GET einzelnes Gutachten
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('GUTACHTER') or hasRole('ADMIN') or hasRole('VERSICHERUNG')")
    @Operation(summary = "Gutachten nach ID abrufen")
    public ResponseEntity<GutachtenResponse> findeNachId(@PathVariable UUID id) {
        return ResponseEntity.ok(abfragenUseCase.findeNachId(id));
    }

    // GET Archivliste (tenant-isoliert)
    @GetMapping
    @PreAuthorize("hasRole('GUTACHTER') or hasRole('ADMIN')")
    @Operation(summary = "Archivliste des aktuellen Tenants")
    public ResponseEntity<List<GutachtenResponse>> findeAlle() {
        return ResponseEntity.ok(abfragenUseCase.findeNachTenant(TenantContextHolder.getTenantId()));
    }

    // GET Gutachten eines Gutachters
    @GetMapping("/gutachter/{gutachterId}")
    @PreAuthorize("hasRole('GUTACHTER') or hasRole('ADMIN')")
    @Operation(summary = "Alle Gutachten eines Gutachters")
    public ResponseEntity<List<GutachtenResponse>> findeNachGutachter(@PathVariable UUID gutachterId) {
        return ResponseEntity.ok(abfragenUseCase.findeNachGutachter(gutachterId));
    }

    // UC-05b: Wiedereinschaltprotokoll
    @PostMapping("/{id}/wiedereinschalten")
    @PreAuthorize("hasRole('GUTACHTER') or hasRole('ADMIN')")
    @Operation(summary = "Wiedereinschaltprotokoll (UC-05b)")
    public ResponseEntity<GutachtenResponse> wiedereinschalten(
            @PathVariable UUID id,
            @Valid @RequestBody WiedereinschaltRequest request) {

        log.info("POST /api/gutachten/{}/wiedereinschalten", id);
        GutachtenResponse response = wiedereinschaltUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    // FA-4.4: PDF-Download
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasRole('GUTACHTER') or hasRole('ADMIN')")
    @Operation(summary = "PDF-Gutachten herunterladen (FA-4.4)")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID id) {
        log.info("GET /api/gutachten/{}/pdf", id);

        var gutachten = abfragenUseCase.findeGutachtenDomain(id);
        byte[] pdf = pdfGenerator.generatePdf(gutachten);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(gutachten.getGutachtenNummer() + "_Gutachten.pdf")
                .build());
        headers.setContentLength(pdf.length);

        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}