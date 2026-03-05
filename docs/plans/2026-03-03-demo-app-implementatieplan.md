# Demo Application — Implementatieplan

> Datum: 2026-03-03

## Implementatie Fasen

### Phase 0: Save design document
- Write design to `docs/plans/2026-03-03-demo-app-design.md`

### Phase 1: Project scaffold
- Create `demo/` directory structure
- Set up `demo/mock-services/` and `demo/simulator/` as standalone Gradle projects with `includeBuild`
- Set up `demo/frontend/` with React + Vite + Tailwind
- Create `demo/docker-compose.yml` extending the base infrastructure
- Verify: each module builds independently

### Phase 2: Mock services
- Implement `MockAuthZenResource` (POST /access/v1/evaluation)
- Implement `MockNotificatieConsumer` (Kafka consumer)
- Implement `SseEventPublisher` for event broadcasting
- Add SmallRye Health checks
- Verify: mock-services starts, health endpoint responds

### Phase 3: Simulator
- Implement `SimulatieService` with `fbs-client-sdk`
- Implement `SimulatieResource` (start/stop/status)
- Implement `EventStreamResource` (SSE)
- Add demo data (organizations, BSNs, realistic subjects)
- Verify: simulator creates messages via SDK, events stream via SSE

### Phase 4: Frontend — System Status + Burger views
- SystemStatus screen with health checks
- Burger InboxView (BSN input → berichtenlijst call)
- Burger BerichtDetail (click message → full content)
- Mark as read functionality
- Verify: can manually browse inbox for a BSN, read a message

### Phase 5: Frontend — Medewerker views
- Org selector + ComposeView form
- Send message → POST to berichtenmagazijn
- Sent messages list
- File attachment upload
- Verify: can compose and send a message as Medewerker

### Phase 6: Frontend — Beheerder + Simulator views
- Stats overview with recharts
- Live event log (SSE from mock-services + simulator)
- Simulator control panel (start/stop, sliders)
- Message flow visualization (animated SVG)
- Verify: can start simulator, see messages flowing in real-time

### Phase 7: Startup script + Docker Compose
- Write `demo.sh` with sequential health-checked startup
- Write `demo/docker-compose.yml` with all services
- Handle profiel-service clone + build
- Add README.md with instructions
- Verify: `./demo/scripts/demo.sh` brings up entire demo from scratch

### Phase 8: Polish
- Rijksoverheid visual styling refinements
- Error states and loading indicators in UI
- Demo data seeder (pre-populate 20 messages for immediate inbox demo)
- Test full demo flow end-to-end

## Files to Create

### New files
- `demo/frontend/package.json` — React + Vite + Tailwind CSS 4 scaffold
- `demo/frontend/vite.config.ts`
- `demo/frontend/src/index.css` — Rijksoverheid color tokens
- `demo/frontend/src/App.tsx` + all view components
- `demo/mock-services/build.gradle.kts`
- `demo/mock-services/settings.gradle.kts` — with `includeBuild` to parent
- `demo/mock-services/src/main/kotlin/nl/rijksoverheid/moz/demo/mock/` — mock resources
- `demo/mock-services/src/main/resources/application.properties`
- `demo/simulator/build.gradle.kts`
- `demo/simulator/settings.gradle.kts` — with `includeBuild` to parent
- `demo/simulator/src/main/kotlin/nl/rijksoverheid/moz/demo/simulator/` — simulator resources
- `demo/simulator/src/main/resources/application.properties`
- `demo/docker-compose.yml` — full stack compose
- `demo/scripts/demo.sh` — startup script
- `demo/README.md` — how to run

### No modifications to existing files
The demo is completely self-contained. Zero changes to existing services, libs, or build files.

## Gradle Integration

Demo modules are **not** included in the main `settings.gradle.kts`. Instead, each has its own `settings.gradle.kts` with `includeBuild("../../")` and `dependencySubstitution` to reference the parent project's libs.

## CORS Configuration

Set via environment variables on existing services (no source changes):
```
QUARKUS_HTTP_CORS=true
QUARKUS_HTTP_CORS_ORIGINS=http://localhost:5173
QUARKUS_HTTP_CORS_METHODS=GET,POST,PATCH,DELETE,OPTIONS
QUARKUS_HTTP_CORS_HEADERS=Content-Type,Authorization,API-Version
```

## Verification

### End-to-end demo test
1. Run `./demo/scripts/demo.sh`
2. Open `http://localhost:5173` → SystemStatus shows all green
3. Navigate to Burger → enter BSN `999999999` → inbox is empty (or pre-seeded)
4. Navigate to Medewerker → select "Belastingdienst" → compose message to BSN 999999999
5. Navigate back to Burger → refresh → new message appears
6. Click message → detail view → click "Markeer als gelezen"
7. Navigate to Beheerder → see stats update
8. Navigate to Simulator → start with 10 users, 5 msg/s → watch flow visualization animate
9. Check Beheerder event log → see simulated events streaming in
