# Federatief Berichtenstelsel (FBS) - Referentie-Implementatie

## Context
Referentie-implementatie van een eigen berichtenmagazijn + FBS Client SDK voor het Federatief Berichtenstelsel (BBO-opdracht, Logius/BZK).

## Tech Stack
- **Taal**: Kotlin (JVM 21)
- **Framework**: Quarkus 3.17.8
- **Build**: Gradle 8.12, Kotlin DSL, version catalog (`gradle/libs.versions.toml`)
- **Database**: PostgreSQL 16 + MinIO (object storage)
- **Messaging**: Apache Kafka (KRaft)
- **Licentie**: EUPL 1.2

## Projectstructuur
```
libs/                  # Herbruikbare libraries (geen Quarkus)
  fbs-common/          # Gedeelde modellen, DTOs, utils
  fbs-client-sdk/      # FBS aansluitbibliotheek
  fbs-authzen-client/  # AuthZEN/FTV client
  fbs-ldv/             # LDV logging (OpenTelemetry)
  fbs-cloudevents/     # CloudEvents NL GOV helpers

services/              # Quarkus microservices
  berichtenmagazijn/   # Core: berichten opslaan/ophalen (port 8080)
  berichtenlijst/      # Aggregatie berichtrecords (port 8081)
  notificatie/         # Multi-channel notificaties (port 8082)
  notificatieprofiel/  # Ontvanger-voorkeuren (port 8083)
  digitale-bereikbaarheid/  # Toestemmingsbeheer (port 8084)
  admin-dashboard/     # Beheer-UI (port 8085)

openapi/               # OpenAPI 3.1 specs (contract-first)
infrastructure/        # Docker Compose, Helm charts
docs/                  # Documentatie en plannen
```

## Build Commands
```bash
# Volledige build (vereist JDK 21)
JAVA_HOME=~/.jdks/ms-21.0.10 ./gradlew build

# Eén module bouwen
./gradlew :services:berichtenmagazijn:build
./gradlew :libs:fbs-common:build

# Tests draaien
./gradlew check

# Quarkus dev mode (enkele service)
./gradlew :services:berichtenmagazijn:quarkusDev

# Alle projecten tonen
./gradlew projects

# Dependencies starten
docker compose -f infrastructure/docker-compose.deps.yml up -d
```

## Conventies
- **API-first**: OpenAPI specs in `openapi/` zijn de bron van waarheid
- **NLGov ADR**: lowerCamelCase, geen trailing slashes, application/problem+json, API-Version header
- **LDV**: OpenTelemetry, OTLP protocol, **geen sampling**
- **Taal**: Nederlands voor API velden en documentatie
- **Tests**: JUnit 5, Testcontainers, MockK

## Relevante Standaarden
- **FSC** - Federated Service Connectivity (mTLS, contracten)
- **FTV** - Federatieve Toegangsverlening (AuthZEN)
- **LDV** - Logboek Dataverwerkingen (OpenTelemetry/OTLP)
- **Digikoppeling REST-API** - Koppelvlakstandaard
- **NLGov API Design Rules** - REST API richtlijnen
- **CloudEvents NL GOV** - Event profiel

## Belangrijke Links
- FSC Core: https://logius-standaarden.github.io/fsc-core/
- AuthZEN NLGov: https://logius-standaarden.github.io/authzen-nlgov/
- LDV: https://logius-standaarden.github.io/logboek-dataverwerkingen/
- NLGov ADR: https://logius-standaarden.github.io/API-Design-Rules/
- Implementatieplan: `docs/plans/implementatieplan.md`
