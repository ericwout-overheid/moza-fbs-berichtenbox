# Bijdragen aan het Federatief Berichtenstelsel

Welkom! We waarderen je interesse in het bijdragen aan de referentie-implementatie van het Federatief Berichtenstelsel. Dit document beschrijft hoe je kunt bijdragen aan het project.

## Gedragscode

Dit project hanteert de [Contributor Covenant](CODE_OF_CONDUCT.md) als gedragscode. Door bij te dragen aan dit project ga je akkoord met de voorwaarden daarvan.

## Hoe kun je bijdragen?

### Bugs melden

- Controleer eerst of het probleem al gemeld is in de [issues](https://github.com/ericwout-overheid/moza-fbs-berichtenbox/issues).
- Maak een nieuw issue aan met een duidelijke titel en beschrijving.
- Voeg stappen toe om het probleem te reproduceren.
- Vermeld je omgeving (OS, JDK-versie, Docker-versie).
- Voeg relevante logbestanden of foutmeldingen toe.

### Features voorstellen

- Open een issue met het label `enhancement`.
- Beschrijf het gewenste gedrag en de motivatie.
- Geef aan hoe de feature past binnen de FBS-standaarden.
- Wacht op feedback voordat je begint met implementatie.

### Code bijdragen

1. Fork de repository.
2. Maak een feature branch aan (zie Branch strategie).
3. Schrijf je code inclusief tests.
4. Zorg dat alle tests slagen.
5. Dien een Pull Request in.

## Development setup

### Vereisten

| Tool | Versie | Doel |
|------|--------|------|
| JDK | 21+ | Kotlin/Quarkus runtime |
| Docker | 24+ | Containers voor lokale services |
| Docker Compose | 2.x | Orchestratie lokale omgeving |
| Gradle | 8.x | Build tool (wrapper meegeleverd) |

### Omgeving opzetten

```bash
# Clone je fork
git clone https://github.com/<jouw-gebruikersnaam>/moza-fbs-berichtenbox.git
cd moza-fbs-berichtenbox

# Start infrastructuur
docker compose -f infrastructure/docker-compose.deps.yml up -d

# Bouw het project
./gradlew build

# Draai de tests
./gradlew test
```

### IDE

We raden IntelliJ IDEA aan met de Kotlin-plugin. Import het project als Gradle-project.

## Branch strategie

| Branch | Doel |
|--------|------|
| `main` | Stabiele, releasable code |
| `feature/<beschrijving>` | Nieuwe functionaliteit |
| `fix/<beschrijving>` | Bugfixes |
| `chore/<beschrijving>` | Onderhoud, configuratie, dependencies |

### Workflow

1. Maak een branch aan vanaf `main`: `git checkout -b feature/mijn-feature`
2. Ontwikkel en commit regelmatig.
3. Push je branch: `git push origin feature/mijn-feature`
4. Open een Pull Request naar `main`.
5. Wacht op review en verwerk feedback.
6. Na goedkeuring wordt de PR gemerged.

## Code stijl

- Volg de officiele [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Gebruik `kotlin.code.style=official` (geconfigureerd in `gradle.properties`).

### Aanvullende richtlijnen

- Schrijf duidelijke KDoc-documentatie voor publieke API's.
- Gebruik betekenisvolle namen voor variabelen, functies en klassen.
- Houd functies kort en gericht op een taak.
- Vermijd onnodige complexiteit.

## Tests

Tests zijn verplicht voor alle nieuwe functionaliteit en bugfixes.

| Type | Framework | Beschrijving |
|------|-----------|-------------|
| Unit tests | JUnit 5 | Testen van individuele componenten |
| Integratietests | Testcontainers | Testen met echte databases en services |
| API tests | REST-assured | Testen van REST endpoints |

### Tests draaien

```bash
# Unit tests
./gradlew test

# Integratietests (vereist Docker)
./gradlew integrationTest

# Alle tests
./gradlew check
```

### Testrichtlijnen

- Schrijf tests voordat je code schrijft (TDD) waar mogelijk.
- Zorg voor een hoge testdekking van businesslogica.
- Gebruik Testcontainers voor integratie met externe systemen.
- Mock externe FBS-services in unit tests.

## Commit conventie

We gebruiken [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <beschrijving>

[optionele body]

[optionele footer]
```

### Types

| Type | Beschrijving |
|------|-------------|
| `feat` | Nieuwe functionaliteit |
| `fix` | Bugfix |
| `docs` | Documentatie |
| `style` | Code formatting (geen functionele wijziging) |
| `refactor` | Code refactoring |
| `test` | Tests toevoegen of wijzigen |
| `chore` | Onderhoud (dependencies, CI, configuratie) |

### Voorbeelden

```
feat(berichtenmagazijn): voeg berichtstatus-endpoint toe

fix(sdk): corrigeer foutafhandeling bij timeout

docs(readme): voeg architectuurdiagram toe

chore(deps): update Quarkus naar 3.x
```

## Licentie

Door bij te dragen aan dit project ga je ermee akkoord dat je bijdragen worden gelicenseerd onder de [European Union Public License v1.2 (EUPL-1.2)](LICENSE). Dit betekent dat je bijdragen vrij beschikbaar zijn onder dezelfde voorwaarden als de rest van het project.

## Vragen?

Heb je vragen over het bijdragen? Open een [discussion](https://github.com/ericwout-overheid/moza-fbs-berichtenbox/discussions) op GitHub.
