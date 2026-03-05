# Demo Application — Design Document

> Datum: 2026-03-03
> Status: Geaccepteerd

## Context

De FBS referentie-implementatie heeft werkende services (berichtenmagazijn, berichtenlijst) en een client SDK, maar geen manier om het systeem te **demonstreren** aan stakeholders (BZK, Logius, geïnteresseerde organisaties). We hebben een demo-applicatie nodig die:

1. Een presentator handmatig door elke gebruikersrol laat lopen (single-user demo)
2. Veel gebruikers kan simuleren om het systeem onder belasting te tonen (visuele traffic generator)
3. In een geïsoleerde `demo/` directory leeft — nul interferentie met de hoofdprojectcode

## Ontwerpbeslissingen

- **Aanpak C**: Aparte frontend + aparte backend services (mock-services, simulator)
- **Geen proxy/BFF**: CORS via omgevingsvariabelen in docker-compose. De echte multi-service architectuur blijft zichtbaar tijdens demo's (verschillende poorten = verschillende services).
- **Echte profiel-service**: Clone MinBZK/moza-profiel-service via startup script (eenvoudig te vervangen door gepubliceerde Docker images later)
- **Visuele stijl**: Tailwind CSS 4, aansluitend bij moza-portaal's Rijksoverheid kleurenpalet (`ro-blue`)
- **Manual-first UI**: Tekstvelden en knoppen voor single-task demo's; simulator is een secundaire feature

## Architectuur

```
demo/
  frontend/                    # React + Vite + TypeScript + Tailwind CSS 4
  mock-services/               # Quarkus (port 8095): AuthZEN + Notificatie mocks
  simulator/                   # Quarkus (port 8092): traffic gen + SSE events
  docker-compose.yml           # Full stack orchestratie
  scripts/demo.sh              # One-command startup met health checks
  README.md
```

### Service Map (tijdens demo)

| Service | Poort | Bron |
|---------|-------|------|
| PostgreSQL (FBS) | 5432 | docker-compose |
| PostgreSQL (Profiel) | 5433 | docker-compose |
| Kafka | 29092 | docker-compose |
| MinIO | 9000/9001 | docker-compose |
| Jaeger | 16686 | docker-compose |
| Berichtenmagazijn | 8080 | dit project (`services/berichtenmagazijn`) |
| Berichtenlijst | 8081 | dit project (`services/berichtenlijst`) |
| Profiel Service | 8088 | gekloond MinBZK/moza-profiel-service |
| Mock Services | 8095 | `demo/mock-services` |
| Simulator | 8092 | `demo/simulator` |
| Frontend (dev) | 5173 | `demo/frontend` (Vite dev server) |

### CORS Configuratie

Via omgevingsvariabelen op bestaande services (geen broncode wijzigingen):
```
QUARKUS_HTTP_CORS=true
QUARKUS_HTTP_CORS_ORIGINS=http://localhost:5173
QUARKUS_HTTP_CORS_METHODS=GET,POST,PATCH,DELETE,OPTIONS
QUARKUS_HTTP_CORS_HEADERS=Content-Type,Authorization,API-Version
```

## Frontend Design

### Views

**System Status** (`/status`)
- Health check grid: pollt `/q/health` op alle services elke 2s
- Rood/groen indicators met service naam en poort
- Blokkeert toegang tot andere views tot alle services groen zijn (met override knop)

**Burger** (`/burger`)
- **Inbox**: Tekstveld voor BSN → roept `GET berichtenlijst:8081/api/v1/berichtenlijst` aan
- **Bericht detail**: Klik een bericht → roept `GET berichtenmagazijn:8080/api/v1/berichten/{id}` aan
- **Markeer als gelezen**: Knop → `PATCH /api/v1/berichten/{id}` met status GELEZEN
- **Zoeken**: Tekstveld voor zoekterm → `GET berichtenlijst:8081/api/v1/berichtenlijst/zoek`

**Medewerker** (`/medewerker`)
- **Organisatie selector**: Dropdown om organisatie te kiezen (Org A / Org B) met verschillende OINs
- **Opstellen**: Formulier met ontvanger type (BSN/KVK/RSIN), ontvanger ID, onderwerp, inhoud → `POST berichtenmagazijn:8080/api/v1/berichten`
- **Verzonden berichten**: Lijst van verzonden berichten voor geselecteerde org
- **Bijlage toevoegen**: Bestand upload → `POST /api/v1/berichten/{id}/bijlagen`

**Beheerder** (`/beheerder`)
- **Statistieken**: Berichttelling per status, berichten per afzender (recharts staaf/taart diagrammen)
- **Event log**: Live SSE feed van mock-services + simulator met Kafka events, auth evaluaties, notificatie pogingen
- **Service health**: Zelfde health grid als /status maar inline

**Simulator** (`/simulator`)
- **Controls**: Start/Stop knop, slider voor gebruikers (10-100), slider voor snelheid (1-20 msg/s), duur invoer
- **Live tellers**: Verstuurd / Gelezen / Gearchiveerd (per seconde + totaal)
- **Berichtstroom visualisatie**: Geanimeerde SVG met nodes (Org A, Org B, Burger) en stippen die tussen hen stromen (framer-motion)
- **Event stream**: Scrollende log van elke gesimuleerde actie

### Tech Stack
- React 18+, TypeScript, Vite
- Tailwind CSS 4 met Rijksoverheid kleur tokens
- recharts voor statistiek diagrammen
- framer-motion voor berichtstroom animatie
- Native EventSource API voor SSE

## Mock Services (poort 8095)

Quarkus service in `demo/mock-services/`:

**AuthZEN Mock** — `POST /access/v1/evaluation`
- Retourneert altijd `{"decision": true}` (configureerbaar via query param `?deny=true`)
- Logt elke evaluatie request naar SSE stream

**Notificatie Mock** — Kafka consumer voor `nl.fbs.bericht-ontvangen`
- Transformeert CloudEvent naar notificatie display record
- Pusht naar SSE stream
- `GET /api/demo/notificaties` — lijst recente notificatie pogingen

**SSE endpoint** — `GET /api/demo/events/stream`
- Broadcast alle mock service activiteit als getypte JSON events

## Simulator (poort 8092)

Quarkus service in `demo/simulator/`:

**SimulatieService** — traffic generation engine:
- Gebruikt `fbs-client-sdk` (`BerichtenClient`) om berichtenmagazijn API aan te roepen
- Actie verdeling: VERSTUUR 60%, LEES 30%, ARCHIVEER 10%
- Demo data: 5 orgs → 20 burger BSNs
- Willekeurige realistische onderwerpen

**REST API:**
- `POST /api/demo/simulatie/start` — body: `SimulatieConfig`
- `POST /api/demo/simulatie/stop`
- `GET /api/demo/simulatie/status` — running/stopped, tellers

**SSE endpoint** — `GET /api/demo/simulatie/events`
- Elke gesimuleerde actie emit: `{type, afzender, ontvanger, onderwerp, timestamp}`

## Gradle Integratie

Demo modules zijn **niet** opgenomen in de hoofd `settings.gradle.kts`. In plaats daarvan hebben `demo/mock-services/` en `demo/simulator/` elk hun eigen `settings.gradle.kts` die de parent project's libs refereren via relatief pad `includeBuild`.

## Implementatie Fasen

1. **Phase 0**: Design document opslaan
2. **Phase 1**: Project scaffold
3. **Phase 2**: Mock services
4. **Phase 3**: Simulator
5. **Phase 4**: Frontend — System Status + Burger views
6. **Phase 5**: Frontend — Medewerker views
7. **Phase 6**: Frontend — Beheerder + Simulator views
8. **Phase 7**: Startup script + Docker Compose
9. **Phase 8**: Polish
