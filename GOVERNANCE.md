# Governance

Dit document beschrijft het governance-model voor het Federatief Berichtenstelsel (FBS) referentie-implementatie project.

## Rollen

| Rol | Verantwoordelijkheid |
|-----|---------------------|
| **Maintainer** | Technische beslissingen, PR-reviews, releases |
| **Contributor** | Code-bijdragen, bug reports, documentatie |

## Besluitvorming

- Technische beslissingen worden genomen door de maintainer(s) via GitHub pull requests.
- Architectuurbeslissingen worden vastgelegd in `docs/` en besproken via GitHub Issues.
- Bij meningsverschillen geldt het [CONTRIBUTING.md](CONTRIBUTING.md) proces.

## Wijzigingsbeheer

1. Wijzigingen worden voorgesteld via GitHub Issues of Pull Requests.
2. Pull requests vereisen minimaal 1 review van een maintainer.
3. De `main` branch is beschermd; directe pushes zijn niet toegestaan.

## Standaarden

Dit project volgt de verplichte standaarden van het [Forum Standaardisatie](https://www.forumstandaardisatie.nl/open-standaarden):

- NLGov API Design Rules (REST API)
- Digikoppeling (koppelvlak)
- CloudEvents NL GOV profiel (notificaties)
- OAuth 2.0 NL profiel / OpenID Connect NL GOV (authenticatie)
- AuthZEN NL GOV (autorisatie)
- Logboek Dataverwerkingen (audit logging)
- FSC (Federated Service Connectivity)

## Licentie

EUPL 1.2 — zie [LICENSE](LICENSE).

## Contact

Zie [SECURITY.md](SECURITY.md) voor beveiligingskwesties.
Zie [CONTRIBUTING.md](CONTRIBUTING.md) voor bijdragen.
