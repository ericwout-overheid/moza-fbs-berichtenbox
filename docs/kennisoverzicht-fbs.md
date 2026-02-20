# Federatief Berichtenstelsel (FBS) - Kennisoverzicht

> Onderzoek uitgevoerd op 13 februari 2026 ter voorbereiding op de BBO-opdracht

---

## 1. Wat is het Federatief Berichtenstelsel?

Het **Federatief Berichtenstelsel (FBS)** is de opvolger van de huidige MijnOverheid Berichtenbox (GLOBE). Het wordt ontwikkeld door **Logius** (agentschap van BZK) en maakt het mogelijk dat overheidsorganisaties berichten aan burgers en ondernemers versturen via gemeenschappelijk gedefinieerde services.

**Kernkenmerken:**
- **Federatief**: Organisaties kunnen een eigen berichtenmagazijn gebruiken OF de centrale BBO
- **ID-neutraal**: Ondersteunt BSN, RSIN, KVK-nummer en toekomstige identifiers
- **Modulair**: Generieke services die individueel of gecombineerd ingezet kunnen worden

**Belangrijke links:**
- [FBS Hoofdpagina](https://www.logius.nl/onze-dienstverlening/interactie/federatief-berichten-stelsel)
- [Wat is het FBS?](https://www.logius.nl/onze-dienstverlening/interactie/federatief-berichten-stelsel/wat-het)
- [Hoe werkt het FBS?](https://www.logius.nl/onze-dienstverlening/interactie/federatief-berichten-stelsel/hoe-werkt-het)
- [Ontwikkeling FBS](https://www.logius.nl/onze-dienstverlening/interactie/federatief-berichten-stelsel/hoe-ontwikkelen-we-het-fbs)
- [Aansluiten op FBS](https://www.logius.nl/domeinen/interactie/federatief-berichten-stelsel/aansluiten)

---

## 2. Architectuur & Componenten

### 2.1 Lagen

| Laag | Beschrijving |
|------|-------------|
| **Interactielaag** | Communicatie met burger/ondernemer (kanalen) |
| **Generieke Services** | Modulaire functionaliteiten (zie hieronder) |
| **Berichtenmagazijnen** | Eigen magazijnen van organisaties + centraal BBO |
| **Afsprakenstelsel** | Standaard operationele protocollen voor deelnemers |

### 2.2 Generieke Services

| Service | Functie |
|---------|---------|
| **Berichtenlijst Service** | Aggregeert berichtrecords met routering naar gedistribueerde opslaglocaties |
| **Notificatie Service** | Multi-channel bezorging (e-mail, SMS, app); afzender bepaalt content en format |
| **Notificatieprofiel Service** | Centraal beheer van voorkeuren van ontvangers |
| **Digitale Bereikbaarheid Service** | Beheert toestemming voor exclusieve digitale communicatie via FBS |

### 2.3 Fasering

- **Fase 1**: Aansluiting op de centrale BBO (vervanging GLOBE)
- **Fase 2**: Aansluiting met eigen berichtenmagazijn

---

## 3. BBO - Berichtenbox voor Burgers en Ondernemers

### 3.1 Opdrachtnemer: CGI

Logius heeft de bouw van BBO gegund aan **CGI** (december 2023). Scope:
- Nieuwbouw BBO
- Migratie van GLOBE naar BBO
- Beheer en doorontwikkeling

**Ontwikkelaanpak:**
- **Cloud-native** ontwikkeling
- **SAFe** (Scaled Agile Framework)
- **Microservices architectuur**
- Teams integreren in Logius' Agile Release Train "Gegevens & Berichten"
- Kwartaal-innovatiesprints inclusief hackathons

**Bronnen:**
- [CGI persbericht](https://www.cgi.com/nl/nl/artikelen/overheid/cgi-gaat-nieuwe-berichtenbox-bouwen-en-beheren)
- [Logius aankondiging](https://www.logius.nl/actueel/logius-gunt-doorontwikkeling-berichtenbox-aan-cgi)
- [Bouwen aan wendbaardere Berichtenbox](https://www.logius.nl/werken-bij-logius/verhalen/bouwen-aan-wendbaardere-berichtenbox)

### 3.2 Technology Stack

Op basis van openbare bronnen:

| Component | Technologie |
|-----------|-------------|
| **Container Platform** | Kubernetes (Logius Standaard Platform) |
| **Event Streaming** | Apache Kafka (via Axual platform, gebaseerd op Strimzi) |
| **CI/CD** | GitLab (standaard op Standaard Platform) |
| **Monitoring** | Prometheus, AlertManager, Grafana |
| **Deployment** | Docker containers op Linux hosts |
| **Methodiek** | SAFe, microservices, cloud-native |

**Bron:** [Axual - Logius Legacy Modernization](https://axual.com/blog/use-case-logius-legacy-modernization)

### 3.3 Logius Standaard Platform

BBO draait op het **Standaard Platform** van Logius - een verplicht container platform voor BZK-organisaties:
- Kubernetes-gebaseerd, open source
- Automatische CI/CD met OTAP-omgevingen (Kubernetes Namespaces)
- Cloud-agnostisch (migratie naar andere K8s-omgevingen mogelijk)
- Standaard beveiligingsrichtlijnen overheid ingeregeld

**Bronnen:**
- [Wat is het Standaard Platform?](https://www.logius.nl/domeinen/infrastructuur/standaard-platform/wat-het-standaard-platform)
- [Aansluitvoorwaarden Kubernetes Applicaties](https://www.logius.nl/diensten/standaard-platform/standaard-platform-documentatie/aansluitvoorwaarden-kubernetes-applicaties)

### 3.4 Migratie GLOBE naar BBO

**Kritische tijdlijn:**
- **16 januari 2026**: Niet meer mogelijk om aan te sluiten op MijnOverheid Berichtenbox (GLOBE)
- **16 jan - 1 aug 2026**: Niet mogelijk om bestaande aansluiting te wijzigen of beëindigen
- **Eind april 2026**: Aansluiten op GLOBE definitief niet meer mogelijk
- **Eind september 2026**: Aansluiting op BBO pre-productieomgeving mogelijk

**Contact migratie:** FBS-migratie@Logius.nl

### 3.5 Berichtenbox Developer Portal

Er bestaat een developer portal voor de (huidige) Berichtenbox API:
- [berichtenbox.dev](https://www.berichtenbox.dev/) - OpenAPI specificaties downloaden, testomgeving aanvragen
- 1 maand testaccess, verlengbaar
- Onboarding-ondersteuning door Logius

**Bestaande koppelvlakken (GLOBE):**
- ebMS koppelvlak
- WUS koppelvlak
- Via Digikoppeling gateway of intermediair

**Technische documentatie:**
- [Technische Aansluithandleiding MijnOverheid Berichtenbox](https://www.logius.nl/domeinen/interactie/mijnoverheid/documentatie/technische-aansluithandleiding-mijnoverheid-berichtenbox)
- [Technische Aansluithandleiding PDF v1.6.3](https://www.logius.nl/sites/default/files/bestanden/website/Technische%20Aansluithandleiding%20MijnOverheid%20Berichtenbox%20v1.6.3.pdf)
- [MijnOverheid Documentatie](https://www.logius.nl/onze-dienstverlening/interactie/voorzieningen/mijnoverheid/documentatie)

---

## 4. De Drie Pijlerstandaarden van het Stelsel

In 2025 zijn drie standaarden ontwikkeld en in beheer genomen die de ruggengraat vormen:

### 4.1 FSC - Federated Service Connectivity

**Wat**: Standaard voor veilige, gestandaardiseerde API-koppelingen tussen organisaties. Beschrijft hoe Peers data uitwisselen via contracten.

**Status**: Goedgekeurd door Programmeringsraad GDI (december 2024)

**Kernarchitectuur:**

| Component | Functie | Poort |
|-----------|---------|-------|
| **Manager** | Contract-onderhandeling, validatie, signatures, access tokens | 8443 |
| **Inway** | Reverse proxy - stelt services beschikbaar (inkomend) | 443 |
| **Outway** | Forward proxy - routeert uitgaande requests | 443 |
| **Directory** | Service- en peer-discovery binnen een Group | 8443 |

**Beveiliging:**
- Mutual TLS (mTLS) met X.509 certificaten
- Certificate-Bound Access Tokens (JWT met x5t#S256, RFC 8705)
- Contract Hashing via JSON Canonicalization Scheme (JCS) + SHA3-512
- PKI-gebaseerd vertrouwen

**Contract Grant Types:**
- `ServicePublicationGrant` - Publiceren van services
- `ServiceConnectionGrant` - Consumeren van services
- `DelegatedServicePublicationGrant` - Publiceren namens andere peer
- `DelegatedServiceConnectionGrant` - Consumeren namens andere peer

**Protocol:** HTTP/1.1 verplicht, HTTP/2 optioneel

**Specificaties & Code:**

| Resource | Link |
|----------|------|
| FSC Core Specificatie (actueel) | [logius-standaarden.github.io/fsc-core](https://logius-standaarden.github.io/fsc-core/) |
| FSC Core v1.1.2 (publicatie) | [gitdocumentatie.logius.nl/publicatie/fsc/core](https://gitdocumentatie.logius.nl/publicatie/fsc/core/) |
| FSC Logging | [logius-standaarden.github.io/fsc-logging](https://logius-standaarden.github.io/fsc-logging/) |
| FSC Properties extensie | [logius-standaarden.github.io/fsc-properties](https://logius-standaarden.github.io/fsc-properties/) |
| GitHub: fsc-core | [github.com/Logius-standaarden/fsc-core](https://github.com/Logius-standaarden/fsc-core) |
| GitHub: fsc-properties | [github.com/Logius-standaarden/fsc-properties](https://github.com/Logius-standaarden/fsc-properties) |
| GitHub: fsc-logging | [github.com/Logius-standaarden/fsc-logging](https://github.com/Logius-standaarden/fsc-logging) |
| FSC Standaard website | [fsc-standaard.nl](https://fsc-standaard.nl/) |

**Referentie-implementatie - OpenFSC:**

| Resource | Link |
|----------|------|
| OpenFSC (referentie-implementatie) | [gitlab.com/commonground/fsc/open-fsc](https://gitlab.com/commonground/fsc/open-fsc) |
| NLX FSC (gearchiveerd) | [gitlab.com/commonground/nlx/fsc-nlx](https://gitlab.com/commonground/nlx/fsc-nlx) |
| FSC ADR's | [gitlab.com/commonground/fsc/adr](https://gitlab.com/commonground/fsc/adr) |

- **Programmeertaal:** Go
- **Licentie:** EUPL 1.2
- **10.486+ commits**, 52+ releases
- Inclusief Inway, Outway, Manager en Directory componenten

**VNG/Digilab:**
- [FSC op Digilab](https://digilab.overheid.nl/projecten/federatieve-service-connectiviteit-fsc/)
- [FSC bij VNG](https://vng.nl/projecten/federatieve-service-connectiviteit-fsc)

### 4.2 FTV - Federatieve Toegangsverlening

**Wat**: Standaard voor autorisatie in federatieve stelsels, gebaseerd op Externalized Authorization Management (EAM).

**Kernstandaard:** NLGov Profile for OpenID AuthZEN Authorization API v1.0
- Gebaseerd op de AuthZEN-standaard van de OpenID Foundation
- In beheer bij Logius (publieke consultatie: okt-nov 2025)
- Leveranciersonafhankelijk en breed inzetbaar

**API Endpoints:**

| Endpoint | Pad | Functie |
|----------|-----|---------|
| Access Evaluation | `/access/v1/evaluation` | Enkele autorisatiebeslissing |
| Access Evaluations | `/access/v1/evaluations` | Batch-beslissingen (boxcarring) |
| Subject Search | `/access/v1/search/subject` | Geautoriseerde subjects zoeken |
| Resource Search | `/access/v1/search/resource` | Toegankelijke resources zoeken |
| Action Search | `/access/v1/search/action` | Toegestane acties zoeken |

**Informatiemodel:**
- **Subject**: Gebruiker/machine principal (type, id, properties)
- **Resource**: Toegangsdoel (type, id, properties)
- **Action**: Type toegang (name, properties)
- **Context**: Omgevingsattributen (time, traceparent, mim, ld-context)
- **Decision**: Autorisatie-uitkomst (decision: boolean, context)

**Nederlandse uitbreidingen:**
- `processing_activity_id` - Koppeling naar verwerkingsactiviteitenregister
- `algorithm_id` - Koppeling naar algoritmeregister
- W3C Trace Context ondersteuning
- MIM (Meta-informatiemodel) URL
- JSON-LD context voor Linked Data

**PDP Discovery:** `/.well-known/authzen-configuration`

**Specificaties & Code:**

| Resource | Link |
|----------|------|
| NLGov AuthZEN profiel | [logius-standaarden.github.io/authzen-nlgov](https://logius-standaarden.github.io/authzen-nlgov/) |
| FTV Website | [vng-realisatie.github.io/ftv](https://vng-realisatie.github.io/ftv/) |
| FTV GitHub | [github.com/VNG-Realisatie/ftv](https://github.com/VNG-Realisatie/ftv) |
| FTV op Digilab | [digilab.overheid.nl/projecten/toegangsverleningmethodiek-api](https://digilab.overheid.nl/projecten/toegangsverleningmethodiek-api/) |
| Logius consultatie | [logius.nl consultatie AuthZEN](https://www.logius.nl/actueel/publieke-consultatie-nlgov-authzen-authorization-api-v10) |

### 4.3 LDV - Logboek Dataverwerkingen

**Wat**: Standaard voor het loggen van dataverwerkingen - wie heeft welke gegevens ingezien, wanneer, en waarom.

**Kerncomponenten:**
- **Applicatie**: Software die dataverwerkingen uitvoert
- **Logboek**: Gespecialiseerde applicatie die logs opslaat
- **Register**: Statische informatie over verwerkingsactiviteiten

**Verplichte logvelden:**

| Veld | Type | Vereist |
|------|------|---------|
| `trace_id` | 16 byte | Ja |
| `span_id` | 8 byte | Ja |
| `status` | enum (Unset/Ok/Error) | Ja |
| `name` | string | Ja |
| `start_time` | uint64 | Ja |
| `end_time` | uint64 | Ja |
| `parent_span_id` | 8 byte | Nee |
| `attributes` | object | Ja |

**Verplichte attributen (namespace `dpl.core`):**
- `processing_activity_id` (URI)
- `data_subject_id` (String, versleuteld)
- `data_subject_id_type` (String)
- `foreign_operation.processor` (URL)

**Protocollen:**
- **OpenTelemetry Protocol (OTLP)** - aanbevolen voor applicatie-naar-logboek communicatie
- **W3C Trace Context** - verplicht voor HTTP/1.1 of HTTP/2 multi-applicatie transacties
- Log Sampling is **VERBODEN** (complete audit trails vereist)

**Detail niveaus:**
1. **Level 1** - Alleen registerreferentie
2. **Level 2** - Register + datacategoriespecificaties
3. **Level 3** - Complete concrete datawaarden

**Specificaties & Code:**

| Resource | Link |
|----------|------|
| LDV Standaard | [logius-standaarden.github.io/logboek-dataverwerkingen](https://logius-standaarden.github.io/logboek-dataverwerkingen/) |
| LDV GitHub | [github.com/Logius-standaarden/logboek-dataverwerkingen](https://github.com/Logius-standaarden/logboek-dataverwerkingen) |
| LDV Inleiding | [github.com/Logius-standaarden/logboek-dataverwerkingen_Inleiding](https://github.com/Logius-standaarden/logboek-dataverwerkingen_Inleiding) |
| LDV Extensie Guidelines | [logius-standaarden.github.io/logboek-extensie-template](https://logius-standaarden.github.io/logboek-extensie-template/) |
| LDV op Digilab | [digilab.overheid.nl/projecten (LDV)](https://digilab.overheid.nl/projecten/) |

---

## 5. Onderliggende Standaarden & Frameworks

### 5.1 Digikoppeling

De logistieke standaard voor berichtenverkeer bij de overheid. FBS bouwt hierop voort.

| Resource | Link |
|----------|------|
| Digikoppeling Architectuur | [logius-standaarden.github.io/Digikoppeling-Architectuur](https://logius-standaarden.github.io/Digikoppeling-Architectuur/) |
| REST-API Koppelvlakstandaard v2.0 | [gitdocumentatie.logius.nl/publicatie/dk/restapi/2.0.0](https://gitdocumentatie.logius.nl/publicatie/dk/restapi/2.0.0/) |
| GitHub: REST-API profiel | [github.com/Logius-standaarden/Digikoppeling-Koppelvlakstandaard-REST-API](https://github.com/Logius-standaarden/Digikoppeling-Koppelvlakstandaard-REST-API) |
| Digikoppeling Documentatie Overzicht | [logius-standaarden.github.io/Digikoppeling-Overzicht-Actuele-Documentatie-en-Compliance](https://logius-standaarden.github.io/Digikoppeling-Overzicht-Actuele-Documentatie-en-Compliance/) |
| Grote Berichten | [logius-standaarden.github.io/Digikoppeling-Koppelvlakstandaard-GB](https://logius-standaarden.github.io/Digikoppeling-Koppelvlakstandaard-GB/) |
| ebMS2 | [logius-standaarden.github.io/Digikoppeling-Koppelvlakstandaard-ebMS2](https://logius-standaarden.github.io/Digikoppeling-Koppelvlakstandaard-ebMS2/) |

**Belangrijk:** FSC wordt opgenomen in het Digikoppeling REST-API profiel. Dit is de convergentie van connectiviteitsstandaarden.

### 5.2 NLGov REST API Design Rules (ADR)

Verplichte standaard ("pas toe of leg uit") voor REST API's bij de overheid.

| Resource | Link |
|----------|------|
| ADR v2.1.0 (actueel) | [gitdocumentatie.logius.nl/publicatie/api/adr](https://gitdocumentatie.logius.nl/publicatie/api/adr/) |
| ADR Werkversie | [logius-standaarden.github.io/API-Design-Rules](https://logius-standaarden.github.io/API-Design-Rules/) |
| Beheermodel | [gitdocumentatie.logius.nl/publicatie/api/adr-beheer](https://gitdocumentatie.logius.nl/publicatie/api/adr-beheer/) |

### 5.3 NL GOV profile for CloudEvents

Standaard voor event-notificaties (bijv. verhuizing, overlijden) - relevant voor de Notificatie Service.

| Resource | Link |
|----------|------|
| CloudEvents NL profiel v1.1 | [logius-standaarden.github.io/NL-GOV-profile-for-CloudEvents](https://logius-standaarden.github.io/NL-GOV-profile-for-CloudEvents/) |
| GitHub | [github.com/Logius-standaarden/NL-GOV-profile-for-CloudEvents](https://github.com/Logius-standaarden/NL-GOV-profile-for-CloudEvents) |
| Guidelines | [logius-standaarden.github.io/CloudEvents-NL-Guidelines](https://logius-standaarden.github.io/CloudEvents-NL-Guidelines/) |

### 5.4 OAuth 2.0 NL GOV Assurance Profile

| Resource | Link |
|----------|------|
| OAuth NL profiel | [logius-standaarden.github.io/OAuth-NL-profiel](https://logius-standaarden.github.io/OAuth-NL-profiel/) |
| Forum Standaardisatie | [forumstandaardisatie.nl OAuth 2.0](https://www.forumstandaardisatie.nl/open-standaarden/nl-gov-assurance-profile-oauth-20) |

---

## 6. Historische Context & Relatie met NLX

### NLX (oorsprong van FSC)

NLX was het oorspronkelijke open-source project voor federatieve API-connectiviteit, ontwikkeld vanuit **VNG/Common Ground**. FSC is de gestandaardiseerde versie hiervan.

| Status | Repository | Link |
|--------|-----------|------|
| **Actief** (referentie-implementatie) | OpenFSC | [gitlab.com/commonground/fsc/open-fsc](https://gitlab.com/commonground/fsc/open-fsc) |
| Gearchiveerd | FSC-NLX | [gitlab.com/commonground/nlx/fsc-nlx](https://gitlab.com/commonground/nlx/fsc-nlx) |
| Gearchiveerd | NLX (origineel) | [gitlab.com/commonground/nlx/nlx](https://gitlab.com/commonground/nlx/nlx) |
| Gearchiveerd | NLX (VNG GitHub) | [github.com/VNG-Realisatie/nlx](https://github.com/VNG-Realisatie/nlx) |

**Technische details OpenFSC:**
- Programmeertaal: **Go**
- Licentie: **EUPL 1.2**
- 10.486+ commits, 52+ releases
- Draait op Kubernetes

---

## 7. GitHub & GitLab Repositories Overzicht

### Logius-standaarden (GitHub)
**[github.com/Logius-standaarden](https://github.com/Logius-standaarden)** - Alle officiële standaarden

Relevante repos:
- `fsc-core`, `fsc-logging`, `fsc-properties` - FSC specificaties
- `logboek-dataverwerkingen` - LDV standaard
- `authzen-nlgov` - FTV/AuthZEN profiel
- `Digikoppeling-*` - Digikoppeling standaarden
- `API-Design-Rules` - REST API Design Rules
- `NL-GOV-profile-for-CloudEvents` - CloudEvents profiel
- `OAuth-NL-profiel` - OAuth 2.0 profiel
- `Openbare-Consultaties` - Openbare consultaties
- `Overleg` - Vergaderstukken/overleg

### Common Ground / FSC (GitLab)
**[gitlab.com/commonground/fsc](https://gitlab.com/commonground/fsc)** - FSC implementaties

- `open-fsc` - Referentie-implementatie (ACTIEF)
- `adr` - Architectural Decision Records

### VNG-Realisatie (GitHub)
**[github.com/VNG-Realisatie](https://github.com/VNG-Realisatie)**

- `ftv` - Federatieve Toegangsverlening website/docs
- `nlx` - NLX (gearchiveerd)
- `common-ground` - Common Ground visiedocument

### LogiusNL (GitHub)
**[github.com/LogiusNL](https://github.com/LogiusNL)** - Slechts 2 kleine repos (niet relevant)

### developer.overheid.nl (GitHub)
**[github.com/developer-overheid-nl](https://github.com/developer-overheid-nl)** - Developer portal overheid

---

## 8. Bredere Context

### Federatief Datastelsel (FDS)

FBS past in het grotere **Federatief Datastelsel** van de Nederlandse overheid:
- [federatief.datastelsel.nl](https://federatief.datastelsel.nl/kennisbank/)
- [FDS Principes](https://federatief.datastelsel.nl/kennisbank/principes/)
- [FDS Basismodel](https://federatief.datastelsel.nl/kennisbank/basismodel/)
- [FDS op developer.overheid.nl](https://developer.overheid.nl/communities/federatief-datastelsel)
- [FDS Werkgroep FTV](https://realisatieibds.nl/groups/view/0056c9ef-5c2e-44f9-a998-e735f1e9ccaa/federatief-datastelsel/wiki/view/d3f8a7ef-f5b0-474d-9018-f49c7f966b08/werkgroep-federatieve-toegangsverlening-ftv)

### Digilab

Innovatiewerkplaats voor de digitale overheid, waar FBS-gerelateerde projecten worden getest:
- [digilab.overheid.nl](https://digilab.overheid.nl/)
- [Digilab projecten](https://digilab.overheid.nl/projecten/)

### NORA

Nederlandse Overheid Referentie Architectuur - het raamwerk waarbinnen FBS opereert:
- [NORA Online](https://www.noraonline.nl/wiki/NORA_online)
- [Verkenning federatief stelsel berichtenverkeer (PDF)](https://www.noraonline.nl/images/noraonline/e/e5/Federatief_stelsel_Berichtenverkeer_emailversie.pdf)

### Developer Overheid

- [developer.overheid.nl](https://developer.overheid.nl/) - Centraal portaal voor overheids-API's

---

## 9. Samenvatting: Wat staat er publiek beschikbaar?

| Categorie | Beschikbaarheid | Details |
|-----------|----------------|---------|
| **FSC Specificatie** | Volledig openbaar | GitHub + gepubliceerde standaard |
| **FSC Referentie-implementatie (OpenFSC)** | Volledig open source (Go, EUPL 1.2) | GitLab, inclusief Inway/Outway/Manager |
| **FTV/AuthZEN specificatie** | Openbaar (in consultatie) | GitHub + gepubliceerde standaard |
| **LDV specificatie** | Volledig openbaar | GitHub + gepubliceerde standaard |
| **Digikoppeling standaarden** | Volledig openbaar | GitHub + gepubliceerde standaarden |
| **API Design Rules** | Volledig openbaar | GitHub + verplichte standaard |
| **CloudEvents NL profiel** | Volledig openbaar | GitHub + gepubliceerde standaard |
| **BBO broncode** | **NIET publiek** | Gebouwd door CGI, geen open source repo |
| **FBS stelselafspraken** | Beperkt openbaar | Hoofdlijnen op Logius website |
| **BBO aansluitdocumentatie** | **Nog niet beschikbaar** | Wordt gepubliceerd zodra gereed |
| **Berichtenbox test-API (GLOBE)** | Beschikbaar via aanvraag | berichtenbox.dev |
| **Technische aansluithandleiding (GLOBE)** | Openbaar (PDF) | Op Logius website |

---

## 10. Aanbevolen Acties voor Maandag

1. **Lees de FSC Core specificatie** - Dit is de technische basis van de connectiviteit
2. **Kloon OpenFSC** (`gitlab.com/commonground/fsc/open-fsc`) - Bestudeer de Go-implementatie van Inway/Outway/Manager
3. **Lees het AuthZEN NLGov profiel** - Begrijp het autorisatiemodel
4. **Lees de LDV standaard** - Begrijp de logging-vereisten (OpenTelemetry)
5. **Bekijk de Digikoppeling REST-API standaard** - Het REST koppelvlak
6. **Vraag toegang aan bij berichtenbox.dev** - Ervaar de huidige API
7. **Neem contact op met Logius** - FBS-migratie@Logius.nl voor actuele BBO documentatie
8. **Bekijk de FSC Standaard website** (fsc-standaard.nl) voor getting-started gidsen

---

## 11. Contactinformatie

| Doel | Contact |
|------|---------|
| Migratie GLOBE -> BBO | FBS-migratie@Logius.nl |
| API standaarden vragen | api@logius.nl |
| Eigen berichtenmagazijn | Logius Business Consultant |
| Berichtenbox test-API | Via berichtenbox.dev |

---

## 12. Digikoppeling - Ontwikkelaarsgids

### 12.1 Overzicht

Digikoppeling is de **verplichte standaard** ("pas toe of leg uit") voor beveiligde gegevensuitwisseling tussen overheidsorganisaties. Het is de logistieke laag waarop het Federatief Berichtenstelsel voortbouwt.

### 12.2 De 4 Koppelvlakprofielen

| Profiel | Wanneer gebruiken |
|---------|------------------|
| **REST-API** | Synchrone request/response, moderne API's — **dit is de toekomst voor FBS** |
| **WUS (SOAP)** | Synchrone bevraging, legacy systemen |
| **ebMS2** | Asynchrone berichten, reliable delivery vereist |
| **Grote Berichten** | Bestanden > 20 MB (aanvullend op bovenstaande) |

**Voor FBS/BBO is het REST-API profiel het meest relevant.** Sinds 1/1/2025 wordt FSC opgenomen in het Digikoppeling REST-API profiel — dit is de convergentie van connectiviteitsstandaarden.

**Profielkeuze beslisboom:**

```
Berichtgrootte > 20 MB?
  JA  --> Grote Berichten (aanvullend op WUS of ebMS2)
  NEE --> Ga verder

Is snelheid/eenvoud belangrijk en past een synchrone request/response?
  JA  --> Is een REST-API beschikbaar/gewenst?
            JA  --> REST-API profiel (met FSC sinds 1/1/2025)
            NEE --> WUS profiel (2W-be)
  NEE --> Ga verder

Is betrouwbare (reliable) aflevering vereist?
  JA  --> ebMS2 reliable profiel (osb-rm)
  NEE --> ebMS2 best-effort profiel (osb-be)
```

### 12.3 Beveiliging — De Kern

**Twee verplichte pijlers:**

1. **PKIoverheid certificaten** — Mutual TLS (mTLS) is verplicht. Beide kanten authenticeren zich met een PKIoverheid X.509 certificaat. Geen self-signed, geen Let's Encrypt.

2. **OIN (Organisatie Identificatie Nummer)** — Elke overheidsorganisatie heeft een uniek 20-cijferig OIN. Dit zit in het PKIoverheid-certificaat en wordt gebruikt voor identificatie en autorisatie.

**TLS eisen (NCSC richtlijnen):**
- Minimaal TLS 1.2, bij voorkeur TLS 1.3
- Sterke cipher suites (ECDHE + AES-256-GCM)

### 12.4 Praktisch: REST-API Implementatie

#### mTLS configureren (Nginx)

De reverse proxy doet de TLS-terminatie en geeft het OIN door als header:

```nginx
server {
    listen 443 ssl;
    server_name api.example.com;

    # PKIoverheid server certificaat
    ssl_certificate     /etc/ssl/pkio/server_cert.pem;
    ssl_certificate_key /etc/ssl/pkio/server_key.pem;

    # mTLS: PKIoverheid client certificaat vereist
    ssl_client_certificate /etc/ssl/pkio/pkio_ca_chain.pem;
    ssl_verify_client on;
    ssl_verify_depth 4;

    # TLS configuratie (NCSC richtlijnen)
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers on;
    ssl_ciphers 'ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384';

    # OIN uit client certificaat doorgeven aan applicatie
    proxy_set_header X-Client-OIN $ssl_client_s_dn_serial;
    proxy_set_header X-Client-CN  $ssl_client_s_dn_cn;

    location /dk/v1/ {
        proxy_pass http://localhost:8080;
    }
}
```

#### OIN valideren en foutafhandeling (Python/FastAPI)

```python
from fastapi import FastAPI, Request, HTTPException
from fastapi.responses import JSONResponse

app = FastAPI(title="Digikoppeling REST-API Voorbeeld", version="1.0.0")

@app.middleware("http")
async def add_digikoppeling_headers(request: Request, call_next):
    """Voeg verplichte Digikoppeling headers toe aan responses."""
    response = await call_next(request)
    response.headers["API-Version"] = "1.0.0"
    response.headers["Strict-Transport-Security"] = "max-age=31536000"
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["Cache-Control"] = "no-store"
    return response

@app.get("/v1/resources/{resource_id}")
async def get_resource(resource_id: str, request: Request):
    """Beveiligd endpoint - mTLS met PKIoverheid certificaat vereist."""
    client_oin = request.headers.get("X-Client-OIN")
    if not client_oin:
        raise HTTPException(status_code=403, detail="OIN niet gevonden in certificaat")
    return {"id": resource_id, "requested_by_oin": client_oin}

@app.exception_handler(HTTPException)
async def problem_json_handler(request: Request, exc: HTTPException):
    """RFC 9457 problem+json foutafhandeling (verplicht per API Design Rules)."""
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "type": f"https://api.example.com/errors/{exc.status_code}",
            "title": exc.detail,
            "status": exc.status_code,
            "instance": str(request.url),
        },
        media_type="application/problem+json"
    )
```

#### Verplichte response headers

| Header | Waarde |
|--------|--------|
| `API-Version` | Versie van de API |
| `Strict-Transport-Security` | `max-age=31536000` |
| `X-Content-Type-Options` | `nosniff` |
| `Cache-Control` | `no-store` |

#### Foutafhandeling (RFC 9457 - verplicht)

```json
{
  "type": "https://api.example.com/errors/oin-unauthorized",
  "title": "OIN niet geautoriseerd",
  "status": 403,
  "detail": "OIN 00000001823288444000 heeft geen toegang tot deze service",
  "instance": "/dk/v1/resources/123"
}
```

### 12.5 Relatie met andere FBS-standaarden

```
Digikoppeling REST-API    ← transportlaag (mTLS + OIN)
    + FSC                 ← federatieve connectiviteit (Inway/Outway, contracten)
    + FTV (AuthZEN)       ← autorisatie (wie mag wat)
    + LDV                 ← logging (wie heeft wat ingezien)
    + NLGov ADR           ← API design regels
```

Digikoppeling regelt het **transport en de identiteit**. FSC voegt daar **federatieve routing en contracten** aan toe. Voor FBS/BBO gebruik je ze samen.

### 12.6 Relevante Documentatie

| Resource | Link |
|----------|------|
| REST-API Koppelvlakstandaard | [Lees online](https://logius-standaarden.github.io/Digikoppeling-Koppelvlakstandaard-REST-API/) |
| Beveiligingsstandaarden | [Lees online](https://logius-standaarden.github.io/Digikoppeling-Beveiligingsstandaarden-en-voorschriften/) |
| Identificatie en Authenticatie | [Lees online](https://logius-standaarden.github.io/Digikoppeling-Identificatie-en-Authenticatie/) |
| PKIoverheid certificaten | [Lees online](https://logius-standaarden.github.io/Digikoppeling-Gebruik-en-achtergrond-certificaten/) |
| OIN-Stelsel | [Lees online](https://logius-standaarden.github.io/OIN-Stelsel/) |
| Architectuur (overkoepelend) | [Lees online](https://logius-standaarden.github.io/Digikoppeling-Architectuur/) |
| Grote Berichten | [Lees online](https://logius-standaarden.github.io/Digikoppeling-Koppelvlakstandaard-GB/) |

---

## 13. CloudEvents & NL GOV Profiel - Ontwikkelaarsgids

### 13.1 Overzicht

CloudEvents is de CNCF-standaard voor het beschrijven van gebeurtenissen (events) op een gestandaardiseerde manier. Het **NL GOV profiel** scherpt deze internationale standaard aan voor de Nederlandse overheid — met specifieke eisen aan naamgeving, identificatie (OIN) en privacy.

CloudEvents is in FBS de basis voor de **Notificatie Service**: het mechanisme waarmee overheidsorganisaties events publiceren (bijv. "persoon verhuisd") en afnemers daarop kunnen reageren.

### 13.2 Verplichte Attributen (NL GOV profiel)

| Attribuut | Type | Voorbeeld |
|-----------|------|-----------|
| `specversion` | String | `"1.0"` |
| `id` | String | UUID v4 of persistent domein-ID (`doc2021033441`) |
| `source` | URN | `urn:nld:oin:00000001823288444000:systeem:BRP-component` |
| `type` | String (RDNN) | `nl.brp.persoon-verhuisd` |

**Regels:**
- `source` moet de **URN nld-notatie** gebruiken: `urn:nld:oin:<OIN>:systeem:<naam>`
- `type` gebruikt **Reverse Domain Name Notation**, met versioning via prefix: `nl.brp.verhuizing.v2`
- De combinatie `source` + `id` moet **globaal uniek** zijn

### 13.3 Optionele Attributen

| Attribuut | Beschrijving |
|-----------|-------------|
| `subject` | Onderwerp van het event (bijv. BSN `999990342`, zaak-ID) — bruikbaar voor filtering zonder payload te openen |
| `time` | RFC 3339 tijdstip van *registratie* (niet per se van de werkelijke gebeurtenis) |
| `datacontenttype` | Media type van de payload, aanbevolen: `application/json` |
| `dataschema` | URI naar het schema van de payload — maakt validatie mogelijk bij ontvangers |
| `dataref` | URI naar externe locatie voor grote/gevoelige payloads (Claim Check Pattern) |
| `sequence` | Relatieve volgorde van events |

### 13.4 Privacy & Beveiliging

> **Belangrijk:** Context-attributen (`source`, `type`, `subject`) zijn zichtbaar voor tussenliggende systemen en worden gelogd. Plaats **nooit persoonsgegevens** in deze velden. Gevoelige data hoort uitsluitend in de versleutelde `data`-payload.

- **TLS verplicht** — minimaal TLS 1.2 voor alle communicatie
- **Versleutel de payload** wanneer deze persoonsgegevens bevat
- **Claim Check Pattern** — gebruik `dataref` voor grote of gevoelige payloads; de ontvanger haalt ze op met eigen autorisatie

### 13.5 Events Publiceren en Consumeren

#### Event publiceren (Python)

```python
from cloudevents.http import CloudEvent
from cloudevents.conversion import to_json
import requests, uuid
from datetime import datetime, timezone

event = CloudEvent({
    "id": str(uuid.uuid4()),
    "source": "urn:nld:oin:00000001823288444000:systeem:BRP-component",
    "type": "nl.brp.persoon-verhuisd",
    "specversion": "1.0",
    "subject": "999990342",         # BSN — geen gevoelige data in attributen!
    "time": datetime.now(timezone.utc).isoformat(),
    "datacontenttype": "application/json",
}, data={
    "oud_adres": {"straat": "Keizersgracht", "huisnummer": "100", "plaats": "Amsterdam"},
    "nieuw_adres": {"straat": "Herengracht", "huisnummer": "200", "plaats": "Amsterdam"},
})

response = requests.post(
    "https://notificaties.example.com/api/v1/notifications",
    headers={
        "Authorization": "Bearer eyJ...",
        "Content-Type": "application/cloudevents+json",
    },
    data=to_json(event),
)
```

#### Webhook ontvanger (FastAPI)

```python
from fastapi import FastAPI, Request, HTTPException
from cloudevents.http import from_http

app = FastAPI(title="CloudEvents Webhook Ontvanger")

@app.post("/webhooks/cloudevents")
async def receive_event(request: Request):
    body = await request.body()
    try:
        event = from_http(dict(request.headers), body)
    except Exception:
        raise HTTPException(400, "Ongeldig CloudEvent formaat")

    # NL GOV profiel validatie
    assert event["specversion"] == "1.0"
    assert event["source"].startswith("urn:nld:")   # URN nld namespace verplicht
    assert "." in event["type"]                      # Reverse Domain Name Notation

    match event["type"]:
        case "nl.brp.persoon-verhuisd":
            await verwerk_verhuizing(event)
        case "nl.kvk.inschrijving-gewijzigd":
            await verwerk_kvk_wijziging(event)

    return {"status": "accepted", "event_id": event["id"]}
```

#### Twee content modes (curl)

```bash
# Structured mode (aanbevolen) — attributen en data samen in JSON body
curl -X POST https://notificaties.example.com/api/v1/notifications \
  -H "Content-Type: application/cloudevents+json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "specversion": "1.0",
    "id": "f3dce042-cd6e-4977-844d-05be8dce7cea",
    "source": "urn:nld:oin:00000001823288444000:systeem:BRP-component",
    "type": "nl.brp.persoon-verhuisd",
    "subject": "999990342",
    "time": "2024-01-15T10:30:00Z",
    "datacontenttype": "application/json",
    "data": {"nieuw_adres": "Herengracht 200, Amsterdam"}
  }'

# Binary mode — attributen als ce-* HTTP headers, body is pure payload
curl -X POST https://notificaties.example.com/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "ce-specversion: 1.0" \
  -H "ce-source: urn:nld:oin:00000001823288444000:systeem:BRP-component" \
  -H "ce-type: nl.brp.persoon-verhuisd" \
  -H "ce-subject: 999990342" \
  -d '{"nieuw_adres": "Herengracht 200, Amsterdam"}'
```

### 13.6 Claim Check Pattern (grote/gevoelige payloads)

Gebruik `dataref` wanneer de payload te groot of te gevoelig is voor het event zelf:

```python
# Producent: verwijst naar extern endpoint
event = CloudEvent({
    "id": str(uuid.uuid4()),
    "source": "urn:nld:oin:00000001823288444000:systeem:DMS",
    "type": "nl.dms.document-beschikbaar",
    "specversion": "1.0",
    "subject": "doc-2024-001",
    "dataref": "https://dms.example.com/api/v1/documents/doc-2024-001",
    # Geen 'data' veld
})

# Consument: haalt payload op met eigen autorisatie
document = requests.get(
    event["dataref"],
    headers={"Authorization": "Bearer receiver_token"},
    cert=("pkio_cert.pem", "pkio_key.pem"),
)
```

### 13.7 Webhook Delivery & Retry

```python
import time, requests

def deliver_event(webhook_url: str, event: dict, max_retries: int = 5):
    """Lever event af aan webhook met exponential backoff."""
    for attempt in range(max_retries):
        try:
            response = requests.post(webhook_url, json=event, timeout=10)
            if response.status_code == 200:
                return True
            if response.status_code >= 500:
                time.sleep(min(2 ** attempt * 5, 300))  # max 5 minuten
                continue
            if response.status_code >= 400:
                return False  # Client error — niet opnieuw proberen
        except requests.Timeout:
            time.sleep(min(2 ** attempt * 5, 300))
    return False
```

### 13.8 Abonneren: Push vs. Pull

| Model | Beschrijving | Wanneer |
|-------|-------------|---------|
| **Push (webhook)** | Notificatieservice levert events actief af aan ontvanger-endpoint | Voorkeur — lage latency, efficiënt |
| **Pull (polling)** | Afnemer bevraagt periodiek de notificatieservice | Wanneer geen inkomend endpoint mogelijk is (firewall) |

### 13.9 Relatie met FBS

In het FBS-stelsel is CloudEvents de standaard voor de **Notificatie Service** en de **Berichtenlijst Service**:

```
Gebeurtenis (bijv. verhuizing in BRP)
    --> CloudEvent publiceren (nl.brp.persoon-verhuisd)
    --> Notificatie Service (NL GOV CloudEvents profiel)
    --> Webhook naar berichtenmagazijn / kanaal (e-mail, app, SMS)
    --> Burger ontvangt bericht via FBS
```

### 13.10 Relevante Documentatie

| Resource | Link |
|----------|------|
| NL GOV profiel voor CloudEvents | [Lees online](https://logius-standaarden.github.io/NL-GOV-profile-for-CloudEvents/) |
| CloudEvents NL Guidelines | [Lees online](https://logius-standaarden.github.io/CloudEvents-NL-Guidelines/) |
| Notificatieservices specificatie | [Lees online](https://logius-standaarden.github.io/Notificatieservices/) |
| Abonneren specificatie | [Lees online](https://logius-standaarden.github.io/Abonneren/) |
| CNCF CloudEvents spec v1.0 | [cloudevents.io](https://cloudevents.io/) |
| Python SDK | `pip install cloudevents` |
| JavaScript SDK | `npm install cloudevents` |
