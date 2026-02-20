# FBS Referentie-Implementatie: Eigen Berichtenmagazijn + Client SDK

## Context

Het Federatief Berichtenstelsel (FBS) vervangt de huidige MijnOverheid Berichtenbox (GLOBE). Logius ontwikkelt de centrale BBO met CGI, maar het FBS-ontwerp voorziet ook in **eigen berichtenmagazijnen** (Fase 2). Er is echter geen open-source referentie-implementatie beschikbaar.

Dit project levert:
1. **Een volledig werkend eigen berichtenmagazijn** - deploybaar op Kubernetes, FBS-compliant
2. **Een FBS client SDK** (Kotlin library) - waarmee organisaties eenvoudig berichten versturen/ontvangen via het FBS

Bronnen: `docs/kennisoverzicht-fbs.md`, FSC Core spec, AuthZEN NLGov, LDV spec, NLGov ADR, CloudEvents NL GOV profiel.

---

## Architectuurkeuzes

| Beslissing | Keuze |
|-----------|-------|
| Taal | Kotlin (JVM 21) |
| Framework | Quarkus 3.x |
| Build | Gradle Kotlin DSL |
| Database | PostgreSQL (metadata) + MinIO (berichtinhoud/bijlagen) |
| Messaging | Apache Kafka (SmallRye Reactive Messaging) |
| FSC | OpenFSC als sidecar containers |
| Auth | Quarkus OIDC + AuthZEN (FTV) client |
| Logging/LDV | OpenTelemetry SDK (OTLP), **geen sampling** |
| API docs | OpenAPI 3.1 (SmallRye OpenAPI) |
| Testing | JUnit 5, Testcontainers, MockK |
| Containers | Docker + Helm charts |
| Dashboard | Vaadin (Kotlin server-side) |
| Architectuur | API-first + Kafka voor async (notificaties, LDV) |
| Projectstructuur | Monorepo (Gradle multi-module) |
| Licentie | EUPL 1.2 |

---

## Projectstructuur

```
federatief-berichtenstelsel/
├── build.gradle.kts                    # Root build met dependency versie-cataloog
├── settings.gradle.kts                 # Module-inclusies
├── gradle/
│   └── libs.versions.toml              # Gradle version catalog
│
├── libs/                               # Herbruikbare libraries
│   ├── fbs-common/                     # Gedeelde modellen, DTOs, utils
│   ├── fbs-client-sdk/                 # FBS aansluitbibliotheek (SDK)
│   ├── fbs-authzen-client/             # AuthZEN/FTV client library
│   ├── fbs-ldv/                        # LDV logging library (OpenTelemetry)
│   └── fbs-cloudevents/                # CloudEvents NL GOV helpers
│
├── services/                           # Microservices
│   ├── berichtenmagazijn/              # Core: berichten opslaan/ophalen
│   ├── berichtenlijst/                 # Aggregatie berichtrecords
│   ├── notificatie/                    # Multi-channel notificatie bezorging
│   ├── notificatieprofiel/             # Ontvanger-voorkeuren beheer
│   ├── digitale-bereikbaarheid/        # Toestemmingsbeheer
│   └── admin-dashboard/               # Vaadin beheer-UI
│
├── infrastructure/
│   ├── docker/                         # Dockerfiles per service
│   ├── docker-compose.yml              # Lokale ontwikkelomgeving
│   ├── docker-compose.deps.yml         # Dependencies (Kafka, PG, MinIO, OpenFSC)
│   └── helm/                           # Helm charts voor K8s deployment
│       ├── Chart.yaml
│       ├── values.yaml
│       └── templates/
│
├── openapi/                            # OpenAPI specificaties (contract-first)
│   ├── berichtenmagazijn-v1.yaml
│   ├── berichtenlijst-v1.yaml
│   ├── notificatie-v1.yaml
│   ├── notificatieprofiel-v1.yaml
│   └── digitale-bereikbaarheid-v1.yaml
│
├── docs/
│   ├── kennisoverzicht-fbs.md
│   └── plans/
│
├── publiccode.yml                      # Standard for Public Code
├── LICENSE                             # EUPL 1.2
├── CONTRIBUTING.md
├── CODE_OF_CONDUCT.md
├── SECURITY.md
├── README.md
├── CLAUDE.md
├── .gitignore
└── .gitlab-ci.yml                      # CI/CD pipeline
```

---

## Service-ontwerp

### 1. Berichtenmagazijn Service (kern)

**Verantwoordelijkheid**: Opslaan, ophalen en beheren van berichten.

**API endpoints** (NLGov ADR-compliant):
```
POST   /api/v1/berichten                    # Bericht aanmaken
GET    /api/v1/berichten                    # Berichten lijst (gepagineerd)
GET    /api/v1/berichten/{id}               # Bericht ophalen
PATCH  /api/v1/berichten/{id}               # Bericht status bijwerken
DELETE /api/v1/berichten/{id}               # Bericht verwijderen
GET    /api/v1/berichten/{id}/bijlagen      # Bijlagen ophalen
POST   /api/v1/berichten/{id}/bijlagen      # Bijlage toevoegen
```

**Datamodel** (PostgreSQL):
- `berichten` tabel: id, afzender_oin, ontvanger_id, ontvanger_id_type (BSN/RSIN/KVK), onderwerp, status, aangemaakt_op, gelezen_op
- Berichtinhoud en bijlagen in MinIO (S3-compatible object storage)
- ID-neutraal: ondersteunt BSN, RSIN, KVK-nummer

**Events** (Kafka, CloudEvents NL GOV):
- `nl.fbs.bericht-ontvangen` - bij nieuw bericht
- `nl.fbs.bericht-gelezen` - bij eerste lezing
- `nl.fbs.bericht-verwijderd` - bij verwijdering

**Cross-cutting**:
- AuthZEN check bij elk endpoint (mag deze OIN berichten sturen aan deze ontvanger?)
- LDV logging bij elke data-verwerking (OTLP spans met `dpl.core` attributen)
- mTLS via OpenFSC Inway (inkomend) / OIN-validatie

### 2. Berichtenlijst Service

**Verantwoordelijkheid**: Aggregeert berichtrecords met routering naar gedistribueerde opslaglocaties.

**API endpoints**:
```
GET    /api/v1/berichtenlijst               # Geaggregeerde lijst voor ontvanger
GET    /api/v1/berichtenlijst/zoek          # Zoeken in berichten
```

**Logica**: Routeert naar het juiste berichtenmagazijn op basis van ontvanger-registratie. Cacht metadata voor snelle lijstweergave.

### 3. Notificatie Service

**Verantwoordelijkheid**: Multi-channel bezorging van notificaties.

**Kafka consumer**: Luistert op `nl.fbs.bericht-ontvangen` events.

**Channels**: Email (SMTP), SMS (gateway), Push (optioneel).

**API endpoints**:
```
POST   /api/v1/notificaties                 # Handmatig notificatie sturen
GET    /api/v1/notificaties/{id}/status      # Bezorgstatus
```

**CloudEvents**: Publiceert `nl.fbs.notificatie-verzonden` events.

### 4. Notificatieprofiel Service

**Verantwoordelijkheid**: Beheer voorkeuren van ontvangers.

**API endpoints**:
```
GET    /api/v1/profielen/{ontvanger_id}      # Voorkeuren ophalen
PUT    /api/v1/profielen/{ontvanger_id}      # Voorkeuren bijwerken
```

**Datamodel**: Kanaalvoorkeuren per ontvanger (email aan/uit, sms aan/uit, frequentie).

### 5. Digitale Bereikbaarheid Service

**Verantwoordelijkheid**: Beheert toestemming voor exclusieve digitale communicatie.

**API endpoints**:
```
GET    /api/v1/bereikbaarheid/{ontvanger_id}  # Toestemming opvragen
PUT    /api/v1/bereikbaarheid/{ontvanger_id}  # Toestemming registreren/intrekken
```

### 6. Admin Dashboard (Vaadin)

**Verantwoordelijkheid**: Beheer-UI voor monitoring en configuratie.

**Views**:
- Berichten overzicht (status, volume, fouten)
- FSC contracten en verbindingen
- Notificatie bezorgstatus
- Systeem health (Kafka lag, service status)
- LDV audit log viewer

---

## Libraries (herbruikbaar)

### fbs-common
Gedeelde Kotlin modellen, DTOs, exception handling (RFC 9457 problem+json), paginering, OIN-validatie.

### fbs-client-sdk
**De FBS aansluitbibliotheek** - het tweede hoofddoel van dit project.

```kotlin
// Gebruik:
val fbsClient = FbsClient.builder()
    .fscOutwayUrl("http://localhost:8080")  // OpenFSC Outway
    .oinAfzender("00000001823288444000")
    .pkiCertificate(cert, key)
    .build()

// Bericht versturen
val bericht = fbsClient.berichten().verstuur(
    ontvangerBsn = "999990342",
    onderwerp = "Belastingaanslag 2025",
    inhoud = "Uw aanslag is beschikbaar...",
    bijlagen = listOf(File("aanslag.pdf"))
)

// Berichten ophalen
val berichten = fbsClient.berichten().lijst(ontvangerBsn = "999990342")

// Notificatie-voorkeuren beheren
fbsClient.profielen().bijwerken(ontvangerBsn = "999990342", email = true, sms = false)
```

### fbs-authzen-client
AuthZEN/FTV client: stuurt access evaluation requests naar PDP.
```kotlin
val decision = authZenClient.evaluate(
    subject = Subject(type = "oin", id = afzenderOin),
    resource = Resource(type = "bericht", id = berichtId),
    action = Action(name = "lezen"),
    context = mapOf("traceparent" to traceContext)
)
```

### fbs-ldv
LDV logging wrapper rond OpenTelemetry SDK.
```kotlin
ldvLogger.logVerwerking(
    processingActivityId = "https://register.example.com/verwerkingen/bericht-opslaan",
    dataSubjectId = encryptedBsn,
    dataSubjectIdType = "BSN",
    operationName = "Bericht opslaan"
)
```

### fbs-cloudevents
CloudEvents NL GOV profiel helpers: event-creatie met correcte URN source, RDNN type.

---

## Standaarden Compliance Matrix

| Standaard | Hoe geimplementeerd |
|-----------|-------------------|
| **NLGov API Design Rules** | OpenAPI 3.1 contract-first, `API-Version` header, semver, `lowerCamelCase`, `application/problem+json` (RFC 9457), geen trailing slash, Nederlands taalgebruik, contactinfo in OAS |
| **Digikoppeling REST-API** | mTLS via OpenFSC/PKIoverheid, OIN-identificatie, security headers (HSTS, X-Content-Type-Options, Cache-Control) |
| **FSC** | OpenFSC sidecar: Inway (inkomend), Outway (uitgaand), Manager (contracten), mTLS met certificate-bound access tokens |
| **FTV / AuthZEN** | AuthZEN PDP client, `/access/v1/evaluation` calls, `processing_activity_id` extensie, W3C Trace Context |
| **LDV** | OpenTelemetry SDK, OTLP protocol, `dpl.core` attributen, **geen sampling**, W3C traceparent propagatie |
| **CloudEvents NL GOV** | Kafka events met `urn:nld:oin:` source, RDNN type (`nl.fbs.*`), structured content mode |
| **OAuth 2.0 NL GOV** | Quarkus OIDC, JWT access tokens, scope-based authorization |
| **Haven** | Containerized (Docker), Helm charts, metrics endpoint (Micrometer/Prometheus), horizontaal schaalbaar |
| **EUPL 1.2** | Licentie in LICENSE bestand |
| **Standard for Public Code** | `publiccode.yml` in root |
| **Open Source** | CONTRIBUTING.md, CODE_OF_CONDUCT.md, SECURITY.md, PROJECT_GOVERNANCE.md |
| **NeRDS Cloud-native** | Containers, CI/CD, IaC (Helm), monitoring (Prometheus/Grafana) |
| **PKIoverheid** | mTLS certificaten via OpenFSC, OIN in X.509 subject |
| **Privacy (AVG)** | BSN versleuteld in LDV logs, geen PII in CloudEvent attributen, data minimalisatie |

---

## Gefaseerde Implementatie

### Fase 0: Project Scaffolding (eerst)
1. Gradle multi-module project opzetten met version catalog
2. Quarkus BOM configuratie
3. Docker Compose voor dependencies (PostgreSQL, MinIO, Kafka, OpenFSC)
4. Basis CI/CD pipeline (.gitlab-ci.yml)
5. Open source bestanden (LICENSE, CONTRIBUTING.md, CODE_OF_CONDUCT.md, publiccode.yml)
6. OpenAPI specificaties schrijven (contract-first)

**Bestanden**:
- `build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`
- `infrastructure/docker-compose.yml`, `infrastructure/docker-compose.deps.yml`
- `openapi/*.yaml`
- `LICENSE`, `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`, `publiccode.yml`, `README.md`
- `.gitlab-ci.yml`, `.gitignore`

### Fase 1: Core Libraries
1. `fbs-common` - Modellen, DTOs, problem+json, paginering, OIN-validatie
2. `fbs-ldv` - LDV logging library (OpenTelemetry wrapper)
3. `fbs-cloudevents` - CloudEvents NL GOV helpers
4. `fbs-authzen-client` - AuthZEN PDP client

**Bestanden**:
- `libs/fbs-common/build.gradle.kts`, `libs/fbs-common/src/main/kotlin/nl/fbs/common/`
- `libs/fbs-ldv/build.gradle.kts`, `libs/fbs-ldv/src/main/kotlin/nl/fbs/ldv/`
- `libs/fbs-cloudevents/build.gradle.kts`, `libs/fbs-cloudevents/src/main/kotlin/nl/fbs/cloudevents/`
- `libs/fbs-authzen-client/build.gradle.kts`, `libs/fbs-authzen-client/src/main/kotlin/nl/fbs/authzen/`

### Fase 2: Berichtenmagazijn Service (kern)
1. PostgreSQL schema (Flyway migraties)
2. MinIO integratie voor berichtinhoud/bijlagen
3. REST API endpoints (contract-first vanuit OpenAPI)
4. Kafka producer (CloudEvents: bericht-ontvangen, bericht-gelezen)
5. AuthZEN integratie
6. LDV logging op alle endpoints
7. Testcontainers-based integration tests

**Bestanden**:
- `services/berichtenmagazijn/build.gradle.kts`
- `services/berichtenmagazijn/src/main/kotlin/nl/fbs/magazijn/`
  - `resource/` (REST controllers)
  - `service/` (business logic)
  - `repository/` (PostgreSQL/Panache)
  - `storage/` (MinIO client)
  - `event/` (Kafka producers)
  - `config/` (Quarkus config)
- `services/berichtenmagazijn/src/main/resources/`
  - `db/migration/` (Flyway)
  - `application.properties`
- `services/berichtenmagazijn/src/test/kotlin/`

### Fase 3: Notificatie + Profiel Services
1. Notificatie Service (Kafka consumer, email/SMS gateway)
2. Notificatieprofiel Service (CRUD API, PostgreSQL)
3. Integratie: bericht -> notificatie pipeline

### Fase 4: Berichtenlijst + Bereikbaarheid
1. Berichtenlijst Service (aggregatie, caching)
2. Digitale Bereikbaarheid Service (toestemmingsbeheer)

### Fase 5: FBS Client SDK
1. `fbs-client-sdk` library bouwen
2. Builder pattern API
3. Integratie met OpenFSC Outway
4. SDK documentatie en voorbeelden

### Fase 6: Admin Dashboard
1. Vaadin applicatie opzetten
2. Berichten overzicht view
3. Systeem monitoring views
4. Notificatie status views

### Fase 7: Infrastructure & Deployment
1. Dockerfiles per service
2. Helm charts
3. Haven compliance checks
4. Monitoring (Prometheus/Grafana dashboards)

---

## Verificatie

### Per service:
- Unit tests (JUnit 5 + MockK): business logic, validatie
- Integration tests (Testcontainers): database, Kafka, MinIO
- Contract tests: OpenAPI spec validatie
- ADR linting: API Design Rules Spectral linter op OpenAPI specs

### End-to-end:
```bash
# 1. Start alle dependencies
docker compose -f infrastructure/docker-compose.deps.yml up -d

# 2. Start services
./gradlew :services:berichtenmagazijn:quarkusDev

# 3. Verstuur een bericht via de API
curl -X POST http://localhost:8080/api/v1/berichten \
  -H "Content-Type: application/json" \
  -d '{"ontvangerBsn":"999990342","onderwerp":"Test","inhoud":"Hallo"}'

# 4. Controleer Kafka events
kafka-console-consumer --topic nl.fbs.bericht-ontvangen

# 5. Controleer LDV spans
# OpenTelemetry collector -> Jaeger UI

# 6. Run Haven compliancy checker op Helm charts
haven-compliancy-checker check ./infrastructure/helm/
```

### CI/CD pipeline:
- `./gradlew check` - alle tests
- `./gradlew buildImage` - Docker images bouwen
- ADR Spectral lint op OpenAPI specs
- OWASP dependency check
- SonarQube/SonarCloud analyse
