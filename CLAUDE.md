# Federatief Berichtenstelsel (FBS) - Project

## Context
Dit project betreft de BBO-opdracht (Berichtenbox voor Burgers en Ondernemers) binnen het Federatief Berichtenstelsel van Logius/BZK.

## Projectstructuur
- `docs/` - Documentatie en kennisoverzichten
- `docs/kennisoverzicht-fbs.md` - Compleet kennisoverzicht FBS/BBO/standaarden

## Relevante Standaarden
- **FSC** (Federated Service Connectivity) - API-connectiviteit tussen organisaties
- **FTV** (Federatieve Toegangsverlening) - Autorisatie via AuthZEN
- **LDV** (Logboek Dataverwerkingen) - Logging via OpenTelemetry/OTLP
- **Digikoppeling REST-API** - Koppelvlakstandaard
- **NLGov API Design Rules** - Verplichte REST API richtlijnen

## Technologie Stack (BBO/Logius)
- Kubernetes (Logius Standaard Platform)
- Apache Kafka (Axual/Strimzi)
- GitLab CI/CD
- Docker containers op Linux
- Microservices architectuur

## Belangrijke Links
- FSC Core spec: https://logius-standaarden.github.io/fsc-core/
- OpenFSC (Go): https://gitlab.com/commonground/fsc/open-fsc
- AuthZEN NLGov: https://logius-standaarden.github.io/authzen-nlgov/
- LDV: https://logius-standaarden.github.io/logboek-dataverwerkingen/
- Berichtenbox dev portal: https://www.berichtenbox.dev/
