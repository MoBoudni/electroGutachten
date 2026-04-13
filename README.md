electroGutachten

KI-Plattform für Hochvolt-Gutachten · E-Mobilität · Java · Spring AI

Die Plattform macht den ganzen Prozess für Hochvolt-Gutachten digital. Das fängt bei der vorgeschriebenen Freischaltung 
nach DGUV 209-093 an, geht über eine automatische Schadensanalyse mit GPT-4 Vision und endet mit einem PDF-Gutachten, 
das allen Regeln entspricht. Dafür braucht man weniger als 45 Minuten, statt wie bisher 4 Stunden.

Ein Diplom-Ingenieur für Fahrzeugtechnik, der sich gut mit Crashsimulationen und FEM auskennt, hat das System entwickelt. 
Das Fachwissen steckt direkt im Programmcode. Das sind Dinge wie Grenzwerte für den Zustand der Batterie (SoH), DGUV-Vorgaben 
und eine Matrix für Reparaturkosten. Diese Regeln sind im Code festgeschrieben und laufen automatisch, anstatt nur als 
Kommentare da zu stehen.

---

Kennzahlen

Kennzahl                Ohne System       Mit electroGutachten
Zeit pro Gutachten      4 Stunden         45 Minuten (70 % weniger)
Gutachten pro Tag       1–2               4–8 (400 % mehr)
Tagesumsatz             180 EUR           Über 900 EUR
GTÜ/DGuSV-Konformität   Manuell           Automatisch durch feste Regeln

---

Funktionen (Version 3.2)

Die Version 3.2 bietet einige nützliche Funktionen:
Zum Beispiel die Diagnose von Hochvolt-Batterien, wo der SoH-Wert, der Restwert-Faktor und der Energieinhalt für NiMH- und 
Li-Ion-Akkus geprüft werden.
Eine KI-Schadensanalyse mit GPT-4 Vision ist auch dabei. Sie erkennt fünf Schadensklassen, gibt eine Sicherheitseinstufung 
(Konfidenz-Score) und nutzt RAG.
Es gibt ein Hochvolt-Sicherheitsprotokoll nach DGUV 209-093, das sieben Prüfschritte und die PSA-Schutzklasse nach EN 60903 
umfasst.
Ein Protokoll zum Wiedereinschalten nach UC-05b und DGUV Vorschrift 3 §6 ist ebenfalls dabei.
Das System erstellt PDF-Gutachten mit iText 8, im PDF 2.0-Format, passend zum GTÜ/DGuSV-Layout und mit einem DSGVO-Audit-Trail.
Dank der Multi-Tenant-Funktion sind Daten komplett voneinander getrennt, das läuft über JWT-Claims und einen TenantContextHolder.
Es gibt eine RAG-Wissensbasis, die auf Skripten von Christiani/Wagner &amp; Teutloff basiert und über PGVector eingebunden ist.
Und falls man keine Cloud nutzen möchte, gibt es eine DSGVO-Offline-Option mit lokaler KI über Ollama.

---

Technologien, die verwendet werden

Komponente          Technologie                     Version
Backend             Spring Boot                     3.2.5
Sprache             Java                            21 (LTS)
Architektur         Hexagonal (Ports &amp; Adapters)
KI-Abstraktion      Spring AI                       1.0.0-M1
KI-Modell           OpenAI GPT-4o Vision ↔ Ollama
Datenbank           PostgreSQL + PGVector           16
ORM                 Spring Data JPA / Hibernate     6.4
Sicherheit          Spring Security + OAuth2 JWT    6.1
API-Dokumentation   SpringDoc OpenAPI               2.5.0
DB-Migration        Flyway                          10.x
PDF                 iText                           8.0.3
Frontend (Q2 2026)  Vaadin                          24.3
Container           Docker + Compose
Reverse Proxy       Traefik                         3.0

---

Aufbau des Projekts

src/main/java/de/electrogutachten/
├── ElectroGutachtenApplication.java
│
├── domain/                          Framework-frei — kein Spring, kein JPA
│   ├── model/
│   │   ├── Gutachten.java           Aggregate Root
│   │   ├── HvSystem.java            Ingenieurwissen als Code
│   │   ├── HvSicherheitsProtokoll.java  FA-3.7/3.8 (EN 60903, DGUV 209-093)
│   │   ├── BatterieAnalyse.java
│   │   ├── KiSchadensAnalyse.java
│   │   ├── Fahrzeug.java
│   │   ├── Gutachter.java
│   │   └── ...
│   ├── valueobject/                 VehicleIdentificationNumber, Enums
│   ├── repository/                  Ports (Interfaces)
│   ├── service/                     GutachtenNummernService
│   └── event/                       Domain-Events
│
├── application/                     Use Cases — reine Orchestrierung
│   ├── usecase/
│   │   ├── GutachtenErstellenUseCase.java      UC-01
│   │   ├── KiAnalyseUseCase.java               UC-03
│   │   ├── GutachtenAbfragenUseCase.java
│   │   └── WiedereinschaltProtokollUseCase.java UC-05b
│   ├── dto/                         Request/Response DTOs
│   └── mapper/                      GutachtenMapper
│
├── infrastructure/                  Adapter
│   ├── ai/
│   │   ├── HvSchadensAnalyseService.java  Spring AI + GPT-4 Vision
│   │   ├── HvPromptBuilder.java           RAG + Fahrzeugkontext
│   │   └── KiAnalyseResultParser.java     JSON-Parsing + Fallback
│   ├── pdf/
│   │   └── GutachtenPdfGenerator.java     iText 8 (FA-4.4)
│   ├── persistence/
│   │   ├── entity/
│   │   │   ├── TenantAwareEntity.java     @MappedSuperclass (ADR-003)
│   │   │   └── GutachtenEntity.java
│   │   ├── jpa/
│   │   │   └── GutachtenJpaRepository.java
│   │   └── GutachtenRepositoryAdapter.java
│   └── security/
│       ├── SecurityConfig.java           @Profile("dev") / @Profile("!dev")
│       └── TenantContextHolder.java      JWT → ThreadLocal → SQL-Filter
│
└── web/
├── controller/
│   └── GutachtenController.java      REST-API
└── advice/
└── GlobalExceptionHandler.java   RFC 7807 ProblemDetail

---

So starten Sie es schnell (lokal)

Was Sie dafür brauchen: Java 21, Maven 3.9 oder neuer, und Docker plus Docker Compose.

1. Als Erstes das Repository klonen:

git clone https://github.com/DEIN-USERNAME/electrogutachten.git
cd electrogutachten

2. Als Nächstes PostgreSQL starten:

docker-compose up -d postgres

Warten Sie, bis der Health-Check grünes Licht gibt:
Dazu geben Sie 'docker-compose ps' ein. Es sollte dann zum Beispiel
eg-postgres   Up (healthy)
anzeigen.

3. Die Umgebungsvariablen einstellen:

Kopieren Sie die Datei '.env.example' zu '.env'.
Öffnen Sie dann die '.env'-Datei und tragen Sie dort Ihren OpenAI API Key ein, zum Beispiel so:
OPENAI_API_KEY=sk-proj-dein-key

4. Das Backend starten (im Entwicklungs-Modus, ohne OAuth2-Pflicht):

mvn spring-boot:run -Dspring-boot.run.profiles=dev

Wenn alles geklappt hat, sehen Sie in der Konsole ungefähr folgende Meldung:
Started ElectroGutachtenApplication in X.XXX seconds

5. Die Swagger UI öffnen:

http://localhost:8080/swagger-ui.html

---

Die API-Zugangspunkte

Methode  Pfad                                     Funktion                              Rolle
POST     /api/gutachten                           Neues Gutachten anlegen (UC-01)       GUTACHTER
POST     /api/gutachten/{id}/analyse              KI-Bildanalyse starten (UC-03)        GUTACHTER
GET      /api/gutachten/{id}                      Gutachten abrufen                     GUTACHTER, VERSICHERUNG
GET      /api/gutachten                           Archivliste (mandantenspezifisch)     GUTACHTER
GET      /api/gutachten/gutachter/{id}            Gutachten eines Gutachters            GUTACHTER
POST     /api/gutachten/{id}/wiedereinschalten    Wiedereinschaltprotokoll (UC-05b)     GUTACHTER
GET      /api/gutachten/{id}/pdf                  PDF-Gutachten herunterladen (FA-4.4)  GUTACHTER
GET      /swagger-ui.html                         API-Dokumentation                     Öffentlich
GET      /actuator/health                         Health-Check                          Öffentlich

---

Tests starten

So können Sie die Tests ausführen:
Für alle Tests:
mvn clean test

Wenn Sie nur die Domain-Tests laufen lassen möchten (die sind sehr schnell und brauchen keinen Spring-Context):
mvn test -Dtest="*Domain*,*HvSystem*,*VehicleIdentification*"

Um einen Bericht über die Testabdeckung zu erhalten:
mvn clean test jacoco:report
Den Bericht finden Sie dann hier: target/site/jacoco/index.html

Ziel für die Testabdeckung: Über 80 % im Domain- und Application-Layer (NFA-06).

---

So wird es eingesetzt (Deployment)

Docker Compose (für lokale Nutzung und den Betrieb):

Um alle Dienste zu starten (Backend, PostgreSQL, Redis, Traefik und Ollama):
docker-compose up -d --build

Wenn Sie nur die Infrastruktur für die Entwicklung starten wollen (z.B. um dann 'mvn spring-boot:run' zu nutzen):
docker-compose up -d postgres redis

Nach dem Start erreichen Sie:
Die App unter: http://localhost:8080
Swagger unter: http://localhost:8080/swagger-ui.html
Das Traefik Dashboard unter: http://localhost:8090

Automatische Bereitstellung (CI/CD mit GitHub Actions):

Die Pipeline startet jedes Mal von selbst, wenn etwas auf 'master' gepusht wird:
Dabei werden Tests mit JUnit 5, einem PostgreSQL Service-Container und JaCoCo durchgeführt.
Danach erfolgt der Build und ein Docker Push zu ghcr.io/DEIN-USERNAME/electrogutachten:latest.
Zuletzt gibt es einen Security-Scan mit OWASP Dependency-Check.

Legen Sie die nötigen Secrets im GitHub Repository an:
OPENAI_API_KEY (hier reicht auch ein Dummy-Key für die Tests)

---

Wichtige Normen und Grundlagen

Norm                 Was sie betrifft
DGUV 209-093         HV-Freischaltprotokoll, PSA-Qualifikationsstufen, Wissensbasis
DGUV Vorschrift 3    Allgemeine Grenzwerte (60 V DC), Wiedereinschaltprotokoll §6
EN 60903             Schutzklassen Isolierhandschuhe (Kl. 0–4)
IEC 60479            Grenzwerte für Körperstrom, Herzflimmern
ISO 3779             VIN-Format (17 Zeichen, WMI/VDS/VIS)
DSGVO (EU) 2016/679  Datenschutz by Design, Art. 9, Art. 25, Art. 30
RFC 7807             HTTP API Fehlerformat (ProblemDetail)

Die fachliche Basis ist das Buch von Henning Wagner, "Alternative Antriebe – E-Mobilität"
(Verlag Europa-Lehrmittel / Christiani) — Das gilt als Standardwerk für die Qualifizierung im Hochvolt-Bereich nach DGUV 209-093.

---

Plan für die Zukunft (Roadmap)

Phase                                               Zeitraum        Status
MVP 1 — Domain, API, Docker, Sicherheitsprotokoll   März 2026       Fertig
MVP 2 — App starten, Swagger, erster API-Call, PDF  Apr–Mai 2026    In Arbeit
MVP 3 — KI live, OpenAI Key, RAG befüllen           Apr–Mai 2026    Geplant
Phase 2 — Vaadin 24 Dashboard                       Q2 2026         Geplant
Phase 3 — SaaS-Launch, OAuth2, AWS/GCP              Q3 2026         Auf der Roadmap

---

Entscheidungen zur Architektur (ADRs)

ADR       Entscheidung                                Begründung
ADR-001   Hexagonale Architektur                      Die Domain ist unabhängig von Frameworks, Adapter können leicht getauscht werden.
ADR-002   Spring AI als KI-Abstraktion                OpenAI oder Ollama können per Konfiguration genutzt werden.
ADR-003   Multi-Tenant über JWT und tenantId-Spalte   Eine Datentrennung auf Datenbank-Ebene wird erreicht.
ADR-004   Domain-Events                               Für eine bessere Entkopplung und Nachvollziehbarkeit.
ADR-005   Flyway für Datenbank-Migrationen            Damit Deployments immer gleich ablaufen.
ADR-006   RFC 7807 ProblemDetail                      Für standardisierte Fehlermeldungen der API.
ADR-007   Stateless API                               Ermöglicht eine einfache horizontale Skalierung.

---

Zum Thema DSGVO

Datenschutz ist von Anfang an mitgedacht (Datenschutz by Design nach Art. 25 DSGVO).
Wir trennen Mandanten über eine 'tenant_id' in allen Tabellen, das wird durch den TenantContextHolder sichergestellt.
Ein Verarbeitungsverzeichnis nach Art. 30 DSGVO ist dokumentiert.
Es gibt eine DSGVO-Offline-Option, bei der Ollama für den Betrieb genutzt wird, ohne dass Daten in die Cloud übertragen werden.
Außerdem gibt es einen Audit-Trail, der alle Zugriffe mit Nutzer-ID und Zeitstempel festhält.

---

electroGutachten Version 3.2 · April 2026
Ingenieur trifft KI trifft E-Mobilität