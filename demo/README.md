# FBS Demo — Federatief Berichtenstelsel

Demo-applicatie voor het Federatief Berichtenstelsel. Demonstreert de volledige
berichtenstroom tussen overheidsorganisaties en burgers.

## Snel starten

```bash
# Start eerst de basis-infrastructuur (als die nog niet draait)
docker compose -f infrastructure/docker-compose.deps.yml up -d

# Start de demo
./demo/scripts/demo.sh
```

Open daarna http://localhost:5173 in je browser.

Het demo script detecteert automatisch of de basis-infrastructuur al draait
en start die indien nodig. Het voegt alleen de extra profiel-database toe
via `demo/docker-compose.yml`.

## Vereisten

- Docker (met Docker Compose v2)
- JDK 21+
- Node.js 18+
- ~4 GB vrij geheugen (voor alle containers + JVM services)

## Services

| Service | Poort | Beschrijving |
|---------|-------|-------------|
| Frontend | 5173 | React demo UI |
| Berichtenmagazijn | 8083 | Core berichten service |
| Berichtenlijst | 8081 | Aggregatie service |
| Profiel Service | 8088 | Organisatie profielen (optioneel) |
| Mock Services | 8095 | AuthZEN + Notificatie mocks |
| Simulator | 8092 | Traffic generator |
| PostgreSQL | 5432 | FBS database |
| PostgreSQL | 5433 | Profiel database |
| Kafka | 29092 | Event streaming |
| MinIO | 9000 | Object storage (bijlagen) |
| Jaeger | 16686 | Distributed tracing |

## Demo flow

1. **Status** — Controleer of alle services draaien
2. **Medewerker** — Selecteer een organisatie en stuur een bericht naar een BSN
3. **Burger** — Voer het BSN in en bekijk het ontvangen bericht
4. **Beheerder** — Bekijk statistieken en live events
5. **Simulator** — Start een simulatie en bekijk de berichtstroom

## Commando's

```bash
# Start alles
./demo/scripts/demo.sh start

# Stop alles
./demo/scripts/demo.sh stop

# Status bekijken
./demo/scripts/demo.sh status

# Herstart
./demo/scripts/demo.sh restart
```

## Architectuur

```
demo/
  frontend/          # React + Vite + Tailwind CSS 4
  mock-services/     # Quarkus: AuthZEN mock + Notificatie mock
  simulator/         # Quarkus: traffic generator + SSE
  docker-compose.yml # Extra infra (profiel PostgreSQL)
  scripts/demo.sh    # Startup script
```

De demo is volledig geïsoleerd van de hoofdproject code. De mock-services en
simulator gebruiken de bestaande libs (`fbs-common`, `fbs-client-sdk`,
`fbs-cloudevents`) via Gradle `includeBuild`.

## Ontwikkeling

Frontend los starten:
```bash
cd demo/frontend
npm install
npm run dev
```

Mock services los starten:
```bash
cd demo/mock-services
./gradlew quarkusDev
```

Simulator los starten:
```bash
cd demo/simulator
./gradlew quarkusDev
```
