package de.electrogutachten.infrastructure.pdf;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfVersion;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import de.electrogutachten.domain.model.Gutachten;
import de.electrogutachten.domain.model.HvSicherheitsProtokoll;
import de.electrogutachten.domain.model.KiSchadensAnalyse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PDF-Generator für HV-Gutachten nach GTÜ/DGuSV-Standard.
 *
 * Technologie: iText 8.0.3, PDF 2.0, DSGVO-konform.
 *
 * Erfüllt FA-4.4 (MVP 2):
 * - GTÜ/DGuSV-konformes Layout
 * - DSGVO-Audit-Trail mit Zeitstempel + Tenant-ID
 * - Sicherheitsprotokoll (DGUV 209-093) als Pflichtbestandteil
 * - PSA-Schutzklasse (FA-3.7) und Wiedereinschaltprotokoll (FA-3.8)
 * - KI-Schadensanalyse mit Konfidenz-Score und Begründung
 * - Reparaturkostenkalkulation
 */
@Slf4j
@Service
public class GutachtenPdfGenerator {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    // electroGutachten Orange: #E8650A
    private static final DeviceRgb ORANGE = new DeviceRgb(0xE8, 0x65, 0x0A);
    private static final DeviceRgb GRAY_LIGHT = new DeviceRgb(0xF1, 0xEF, 0xE8);
    private static final DeviceRgb TEAL = new DeviceRgb(0x0F, 0x6E, 0x56);

    /**
     * Generiert ein vollständiges PDF-Gutachten als Byte-Array.
     *
     * @param gutachten Das finalisierte Gutachten-Aggregat (Status FERTIG)
     * @return PDF als byte[] — direkt als HTTP-Response downloadbar
     */
    public byte[] generatePdf(Gutachten gutachten) {
        log.info("Generiere PDF für Gutachten {}", gutachten.getGutachtenNummer());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // PDF 2.0 — revisionssicher, DSGVO-konform
            WriterProperties props = new WriterProperties()
                    .setPdfVersion(PdfVersion.PDF_2_0)
                    .setFullCompressionMode(true);

            PdfWriter writer = new PdfWriter(baos, props);
            PdfDocument pdf  = new PdfDocument(writer);
            Document   doc   = new Document(pdf);

            PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // ── HEADER ──────────────────────────────────────────────
            doc.add(new Paragraph("electroGutachten")
                    .setFont(bold).setFontSize(22)
                    .setFontColor(ORANGE)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph("Hochvolt-Gutachten · GTÜ/DGuSV-konform · DGUV 209-093")
                    .setFont(regular).setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph("\n"));

            // ── GUTACHTEN-STAMMDATEN ─────────────────────────────────
            doc.add(sectionHeader("Gutachten-Stammdaten", bold));
            Table stamm = twoColTable();
            addRow(stamm, "Gutachtennummer",  gutachten.getGutachtenNummer(), bold, regular);
            addRow(stamm, "Erstellt am",      fmt(gutachten.getErstelltAm()), bold, regular);
            addRow(stamm, "Status",           gutachten.getStatus().name(),   bold, regular);
            doc.add(stamm);
            doc.add(new Paragraph("\n"));

            // ── FAHRZEUGDATEN ────────────────────────────────────────
            doc.add(sectionHeader("Fahrzeugdaten", bold));
            Table fz = twoColTable();
            addRow(fz, "VIN (ISO 3779)",   gutachten.getFahrzeug().getVin().toString(),         bold, regular);
            addRow(fz, "Fahrzeug",         gutachten.getFahrzeug().getAnzeigename(),            bold, regular);
            addRow(fz, "HV-Nennspannung",  gutachten.getFahrzeug().getHvSystem().getNennspannung() + " V", bold, regular);
            addRow(fz, "Batterietyp",      gutachten.getFahrzeug().getHvSystem().getBatterieTyp().name(), bold, regular);
            addRow(fz, "Energieinhalt",
                    String.format("%.2f kWh", gutachten.getFahrzeug().getHvSystem().getEnergieinhaltKwh()),
                    bold, regular);
            doc.add(fz);
            doc.add(new Paragraph("\n"));

            // ── HV-SICHERHEITSPROTOKOLL (DGUV 209-093) ─────────────
            doc.add(sectionHeader("HV-Sicherheitsprotokoll (DGUV 209-093 / DGUV V3)", bold));
            HvSicherheitsProtokoll p = gutachten.getSicherheitsProtokoll();
            if (p != null) {
                Table proto = twoColTable();
                addRow(proto, "HV freigeschaltet",     jaNein(p.isHvFreigeschaltet()),         bold, regular);
                addRow(proto, "PSA angelegt",           jaNein(p.isPsaAngelegt()),              bold, regular);
                addRow(proto, "HV-Service-Plug gezogen",jaNein(p.isHvServicePlugEntfernt()),   bold, regular);
                addRow(proto, "5-Min. Wartezeit",       jaNein(p.isWartezeit5MinEingehalten()), bold, regular);
                addRow(proto, "Spannung gemessen",
                        String.format("%.1f V (Grenzwert: < 60 V DC)", p.getGemesseneSpannungV()), bold, regular);
                addRow(proto, "Isolationswiderstand",
                        String.format("%.1f kΩ (Grenzwert: ≥ 100 kΩ)", p.getIsolationswiderstandKOhm()), bold, regular);
                addRow(proto, "Bereich abgesperrt",     jaNein(p.isBereichAbgesperrt()),        bold, regular);
                addRow(proto, "EMV-Prüfung",            jaNein(p.isEmvPruefungAbgeschlossen()), bold, regular);

                // FA-3.7: PSA-Schutzklasse (EN 60903)
                String klasse = p.getHandschuhSchutzklasse() >= 0
                        ? "Klasse " + p.getHandschuhSchutzklasse() + " (EN 60903)" : "Nicht erfasst";
                addRow(proto, "PSA-Schutzklasse (FA-3.7)", klasse, bold, regular);
                addRow(proto, "Handschuh geprüft (Lufttest)", jaNein(p.isHandschuhGeprueft()), bold, regular);

                // FA-3.8: Wiedereinschaltprotokoll
                addRow(proto, "Wiedereinschalt-Freigabe (FA-3.8)", jaNein(p.isWiedereinschaltFreigabe()), bold, regular);
                addRow(proto, "Wiedereinschalt-Zeitpunkt",
                        p.getWiedereinschaltZeitpunkt() != null ? fmt(p.getWiedereinschaltZeitpunkt()) : "Noch nicht eingeschaltet",
                        bold, regular);
                addRow(proto, "Vollständigkeit",
                        p.getVollstaendigkeitProzent() + " %", bold, regular);
                doc.add(proto);
            } else {
                doc.add(new Paragraph("⚠ Kein Sicherheitsprotokoll vorhanden")
                        .setFont(bold).setFontColor(ColorConstants.RED));
            }
            doc.add(new Paragraph("\n"));

            // ── BATTERIE-ANALYSE ─────────────────────────────────────
            if (gutachten.getBatterieAnalyse() != null) {
                doc.add(sectionHeader("Batterie-Analyse", bold));
                var b = gutachten.getBatterieAnalyse();
                Table bat = twoColTable();
                addRow(bat, "State of Health (SoH)",
                        String.format("%.1f %% → %s", b.getSohProzent(), b.getBewertung().name()), bold, regular);
                addRow(bat, "Isolationswiderstand",
                        String.format("%.1f kΩ %s", b.getIsolationswiderstandKOhm(),
                                b.isIsolationKritisch() ? "⚠ KRITISCH" : "✓ OK"), bold, regular);
                addRow(bat, "Restwert",
                        String.format("%.2f EUR", b.getRestwertEur()), bold, regular);
                doc.add(bat);
                doc.add(new Paragraph("\n"));
            }

            // ── KI-SCHADENSANALYSE ────────────────────────────────────
            if (gutachten.getKiSchadensAnalyse() != null) {
                doc.add(sectionHeader("KI-Schadensanalyse (GPT-4 Vision)", bold));
                KiSchadensAnalyse ki = gutachten.getKiSchadensAnalyse();
                Table kiTbl = twoColTable();
                addRow(kiTbl, "Hauptschadentyp",  ki.getHauptSchadenTyp().name(),          bold, regular);
                addRow(kiTbl, "Konfidenz-Score",   ki.getKonfidenzAlsProzent(),             bold, regular);
                addRow(kiTbl, "Validierung nötig", jaNein(ki.brauchtValidierung()),         bold, regular);
                addRow(kiTbl, "Erkannte Komponenten",
                        String.join(", ", ki.getErkannteHvKomponenten()),                       bold, regular);
                addRow(kiTbl, "KI-Begründung",     ki.getKiBegruendung(),                   bold, regular);
                doc.add(kiTbl);
                doc.add(new Paragraph("\n"));
            }

            // ── REPARATURKALKULATION ──────────────────────────────────
            if (gutachten.getReparaturkostenEur() != null) {
                doc.add(sectionHeader("Reparaturkalkulation", bold));
                doc.add(new Paragraph(
                        "Reparaturkosten (kalkuliert): " + gutachten.getReparaturkostenEur() + " EUR")
                        .setFont(bold).setFontSize(14).setFontColor(ORANGE));
                doc.add(new Paragraph("\n"));
            }

            // ── DSGVO-AUDIT-TRAIL ─────────────────────────────────────
            doc.add(new Paragraph(
                    "Erstellt durch: electroGutachten v3.2 · " + fmt(LocalDateTime.now()) +
                            " · Tenant: " + (gutachten.getGutachter() != null
                            ? gutachten.getGutachter().getTenantId() : "n/a") +
                            " · Gutachter: " + (gutachten.getGutachter() != null
                            ? gutachten.getGutachter().getAnzeigename() : "n/a"))
                    .setFont(regular).setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph(
                    "DSGVO-konform · GTÜ/DGuSV-Standard · DGUV 209-093 · Norm: EN 60903")
                    .setFont(regular).setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.close();

        } catch (IOException e) {
            log.error("Fehler bei PDF-Generierung für Gutachten {}", gutachten.getGutachtenNummer(), e);
            throw new RuntimeException("PDF-Generierung fehlgeschlagen: " + e.getMessage(), e);
        }

        log.info("PDF für Gutachten {} generiert ({} Bytes)",
                gutachten.getGutachtenNummer(), baos.size());
        return baos.toByteArray();
    }

    // ── Hilfsmethoden ──────────────────────────────────────────────

    private Paragraph sectionHeader(String title, PdfFont font) {
        return new Paragraph(title)
                .setFont(font).setFontSize(12)
                .setFontColor(TEAL)
                .setBorderBottom(new SolidBorder(TEAL, 0.5f))
                .setMarginBottom(4);
    }

    private Table twoColTable() {
        Table t = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .useAllAvailableWidth();
        return t;
    }

    private void addRow(Table table, String label, String value, PdfFont bold, PdfFont regular) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFont(bold).setFontSize(9))
                .setBackgroundColor(GRAY_LIGHT)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.3f)));
        table.addCell(new Cell()
                .add(new Paragraph(value != null ? value : "—").setFont(regular).setFontSize(9))
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.3f)));
    }

    private String jaNein(boolean b) { return b ? "JA ✓" : "NEIN ✗"; }

    private String fmt(LocalDateTime dt) {
        return dt != null ? dt.format(DATE_FMT) : "—";
    }
}
