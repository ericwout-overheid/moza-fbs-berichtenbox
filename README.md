# Federatief Berichtenstelsel - Referentie-Implementatie

[![CI](https://github.com/ericwout-overheid/moza-fbs-berichtenbox/actions/workflows/ci.yml/badge.svg)](https://github.com/ericwout-overheid/moza-fbs-berichtenbox/actions/workflows/ci.yml)
[![License: EUPL 1.2](https://img.shields.io/badge/License-EUPL%201.2-blue.svg)](LICENSE)

Referentie-implementatie van een **eigen berichtenmagazijn** voor het Federatief Berichtenstelsel (FBS), inclusief een FBS Client SDK. Dit project laat zien hoe een overheidsorganisatie een eigen FBS-compliant berichtenmagazijn kan bouwen en berichten kan uitwisselen binnen het stelsel. Het berichtenmagazijn biedt opslag, routering en notificatie van berichten aan burgers en ondernemers, conform de standaarden van Logius/BZK.

## Kenmerken

- **Eigen berichtenmagazijn** -- FBS-compliant berichtenopslag en -beheer
- **FBS Client SDK** -- Kotlin-bibliotheek voor integratie met het FBS
- **NLGov API Design Rules** -- Volledig conform de verplichte REST API-richtlijnen
- **FSC (Federated Service Connectivity)** -- Federatieve API-connectiviteit via OpenFSC
- **LDV (Logboek Dataverwerkingen)** -- Audit logging conform OpenTelemetry/OTLP
- **CloudEvents NL GOV profiel** -- Gestandaardiseerde event-notificaties
- **Haven/Kubernetes deployment ready** -- Containerized voor het Logius Standaard Platform

## Architectuur

Het project bestaat uit de volgende componenten:

```
federatief-berichtenstelsel/
  services/
    berichtenmagazijn/        # Eigen berichtenmagazijn (core service)
    berichtenlijst/           # Berichtenlijst Service (aggregatie & routering)
    notificatie/              # Notificatie Service (CloudEvents, multi-channel)
    notificatieprofiel/       # Notificatieprofiel Service (voorkeuren ontvangers)
    digitale-bereikbaarheid/  # Digitale Bereikbaarheid Service
    admin-dashboard/          # Beheer dashboard
  libs/
    fbs-client-sdk/           # FBS Client SDK (Kotlin library)
    fbs-common/               # Gedeelde modellen en utilities
    fbs-authzen-client/       # FTV/AuthZEN autorisatie client
    fbs-ldv/                  # Logboek Dataverwerkingen integratie
    fbs-cloudevents/          # CloudEvents NL GOV profiel
  openapi/                    # OpenAPI specificaties
```

### Services

| Service | Beschrijving |
|---------|-------------|
| **Berichtenmagazijn** | Kern berichtenopslag: berichten ontvangen, opslaan, doorzoeken en ophalen |
| **Berichtenlijst** | Aggregeert berichtrecords met routering naar gedistribueerde opslaglocaties |
| **Notificatie** | Multi-channel notificaties (e-mail, SMS, app) via CloudEvents |
| **Notificatieprofiel** | Centraal beheer van notificatievoorkeuren van ontvangers |
| **Digitale Bereikbaarheid** | Beheer van toestemming voor digitale communicatie via FBS |
| **Admin Dashboard** | Beheer- en monitoringinterface |

### Bibliotheken

| Library | Beschrijving |
|---------|-------------|
| **FBS Client SDK** | Kotlin-bibliotheek voor het aanspreken van FBS-diensten |
| **FBS Common** | Gedeelde domeinmodellen, DTOs en hulpfuncties |
| **FBS AuthZEN Client** | Client voor FTV/AuthZEN autorisatiebeslissingen |
| **FBS LDV** | Logboek Dataverwerkingen integratie (OpenTelemetry/OTLP) |
| **FBS CloudEvents** | CloudEvents NL GOV profiel implementatie |

## Snel starten

### Vereisten

- JDK 21+
- Docker en Docker Compose
- Gradle 8.x (of gebruik de meegeleverde Gradle wrapper)

### Lokale ontwikkelomgeving starten

```bash
# Clone de repository
git clone https://github.com/ericwout-overheid/moza-fbs-berichtenbox.git
cd moza-fbs-berichtenbox

# Start de infrastructuur (PostgreSQL, MinIO, Kafka)
docker compose -f infrastructure/docker-compose.deps.yml up -d

# Bouw het project
./gradlew build

# Start het berichtenmagazijn
./gradlew :services:berichtenmagazijn:quarkusDev
```

De API is beschikbaar op `http://localhost:8080/api/v1/`.

### Tests uitvoeren

```bash
# Alle tests
./gradlew test

# Integratietests (vereist Docker)
./gradlew integrationTest
```

## Technologie Stack

| Component | Technologie | Versie |
|-----------|-------------|--------|
| Taal | Kotlin | 2.x |
| Framework | Quarkus | 3.x |
| Database | PostgreSQL | 16 |
| Objectopslag | MinIO (S3-compatible) | latest |
| Event streaming | Apache Kafka | 3.x |
| Federatieve connectiviteit | OpenFSC | latest |
| Build tool | Gradle (Kotlin DSL) | 8.x |
| Container runtime | Docker / Podman | - |
| Orchestratie | Kubernetes / Haven | - |

## Standaarden

Dit project implementeert de volgende overheidsstandaarden:

| Standaard | Beschrijving | Link |
|-----------|-------------|------|
| **FSC** | Federated Service Connectivity | [Specificatie](https://logius-standaarden.github.io/fsc-core/) |
| **FTV** | Federatieve Toegangsverlening (AuthZEN) | [Specificatie](https://logius-standaarden.github.io/authzen-nlgov/) |
| **LDV** | Logboek Dataverwerkingen | [Specificatie](https://logius-standaarden.github.io/logboek-dataverwerkingen/) |
| **Digikoppeling** | REST-API Koppelvlakstandaard | [Specificatie](https://logius-standaarden.github.io/Digikoppeling-Koppelvlakstandaard-REST-API/) |
| **NLGov ADR** | API Design Rules | [Specificatie](https://gitdocumentatie.logius.nl/publicatie/api/adr/) |
| **CloudEvents** | NL GOV profiel voor CloudEvents | [Specificatie](https://logius-standaarden.github.io/NL-GOV-profile-for-CloudEvents/) |

## Bijdragen

Bijdragen zijn welkom! Lees de [CONTRIBUTING.md](CONTRIBUTING.md) voor richtlijnen over hoe je kunt bijdragen aan dit project.

## Licentie

Dit project is gelicenseerd onder de [European Union Public License v1.2 (EUPL-1.2)](LICENSE).

## Contact

- **Project**: [github.com/ericwout-overheid/moza-fbs-berichtenbox](https://github.com/ericwout-overheid/moza-fbs-berichtenbox)
- **Organisatie**: Moza
- **FBS informatie**: [Logius - Federatief Berichtenstelsel](https://www.logius.nl/onze-dienstverlening/interactie/federatief-berichten-stelsel)
- **Developer portal**: [berichtenbox.dev](https://www.berichtenbox.dev/)
