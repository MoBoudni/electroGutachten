package de.electrogutachten.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

// 🔥 IMPORT FIX!
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("electroGutachten – Hochvolt-Gutachten Dashboard")
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setHorizontalComponentAlignment(Alignment.CENTER);

        // Header
        add(new H1("⚡ electroGutachten"));
        add(new Paragraph("KI-Plattform für Hochvolt-Gutachten | E-Mobilität | LIVE MVP"));

        // Stats Cards
        HorizontalLayout stats = new HorizontalLayout();
        stats.setSpacing(true);
        stats.setWidthFull();
        stats.setJustifyContentMode(JustifyContentMode.CENTER);  // ← Jetzt grün!
        stats.add(
                buildStatCard("Gutachten heute", "0", "var(--lumo-primary-color)"),
                buildStatCard("Ø Bearbeitungszeit", "2.3 min", "var(--lumo-success-color)"),
                buildStatCard("HV-Analysen gesamt", "47", "var(--lumo-contrast)")
        );
        add(stats);

        // KFZ Formular
        H3 formTitle = new H3("Neues Gutachten erstellen");
        add(formTitle);

        TextField kennzeichen = new TextField("Kennzeichen", "z.B. K-AA 1234");
        kennzeichen.setWidth("300px");

        DatePicker prüfdatum = new DatePicker("Prüfdatum");
        prüfdatum.setWidth("300px");

        Button pdfErstellen = new Button("📄 Gutachten PDF erstellen");
        pdfErstellen.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        pdfErstellen.addClickListener(e -> {
            Notification.show("✅ PDF wird generiert (iText 8.0.4)...", 3000, Notification.Position.MIDDLE);
        });

        HorizontalLayout form = new HorizontalLayout(kennzeichen, prüfdatum, pdfErstellen);
        form.setSpacing(true);
        form.setAlignItems(Alignment.END);
        add(form);

        add(new H3("✅ MVP Schritt 1 erfolgreich – LIVE!"));
        add(new Paragraph("Spring Boot 3.2.5 + Vaadin 24.3 + H2 + iText"));
    }

    private VerticalLayout buildStatCard(String label, String value, String color) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("min-width", "200px")
                .set("box-shadow", "var(--lumo-box-shadow-xs)");

        Paragraph val = new Paragraph(value);
        val.getStyle().set("font-size", "2.5em").set("font-weight", "bold").set("color", color).set("margin", "0");
        Paragraph lbl = new Paragraph(label);
        lbl.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin", "0").set("font-size", "0.9em");

        card.add(val, lbl);
        return card;
    }
}