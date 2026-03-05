# ADR-0001: Alleen REST API als inbound protocol, geen ebMS

**Status:** Accepted
**Datum:** 2026-03-05
**Beslissers:** FBS-team

## Context

Overheidsorganisaties leveren berichten aan bij de BBO (Berichtenbox voor Burgers en Ondernemers). Dit is inter-organisationeel verkeer waarvoor Digikoppeling van toepassing is. Digikoppeling biedt drie koppelvlakprofielen:

1. **REST API** — voor bevragingen en operaties op dataresources
2. **ebMS2** — voor betrouwbare asynchrone berichtaflevering (meldingen)
3. **WUS** — voor gestructureerde SOAP-berichten

Het afleveren van een bericht aan BBO is functioneel een *melding* — een patroon waarvoor ebMS traditioneel het aangewezen Digikoppeling-profiel is. Tegelijkertijd:

- **ebMS2 is end-of-life**: de standaard wordt niet meer doorontwikkeld en markondersteuning neemt af ([Digikoppeling Roadmap 2024-2025](https://gitdocumentatie.logius.nl/publicatie/dk/roadmap/2024-2025/))
- **ebMS3/AS4** is de beoogde opvolger (MIDO-besluit), maar de migratie bij overheidsorganisaties is nog gaande
- **Digikoppeling REST API** is sinds 2020 een volwaardig koppelvlakprofiel en mag door de provider worden gekozen
- De huidige applicatiearchitectuur is volledig REST-gebaseerd met een schone scheiding tussen transport- en domeinlaag

## Besluit

We implementeren **uitsluitend het REST API-koppelvlak** voor het ontvangen van berichten. We implementeren geen ebMS-ondersteuning (ebMS2 noch ebMS3/AS4) in de applicatie.

## Overwegingen

### Waarom geen ebMS in de applicatie?

| Aspect | Impact van ebMS-ondersteuning |
|---|---|
| **MSH-infrastructuur** | Vereist een MSH (Message Service Handler), hetzij embedded (bijv. Holodeck B2B, phase4) hetzij standalone — aanzienlijke operationele complexiteit |
| **Dubbele authenticatie** | REST gebruikt JWT/OAuth 2.0; ebMS gebruikt WS-Security met PKIoverheid-certificaten — twee identity-extractiepaden nodig |
| **Contractbeheer** | REST heeft OpenAPI-specs; ebMS vereist CPA's (Collaboration Protocol Agreements) per organisatie + WSDL — dubbel contractbeheer |
| **Bijlagen** | REST gebruikt multipart/form-data; ebMS gebruikt SOAP with Attachments (SwA) — twee uploadpaden |
| **Foutafhandeling** | REST retourneert RFC 9457 ProblemDetail; ebMS retourneert SOAP Faults + ebMS Error signals — dubbele foutmapping |
| **Testoppervlak** | Elk scenario moet in beide protocolvarianten getest worden |
| **Dependencies** | SOAP/CXF/MSH-libraries zijn zwaar en voegen aanzienlijk toe aan de build |

### Waarom is dit verantwoord?

1. **De servicelaag is transport-agnostisch**: `BerichtService` werkt met domein-DTOs (`BerichtAanmaakVerzoek`, `Bericht`), niet met HTTP-objecten. Een toekomstig ebMS-ingangskanaal kan dezelfde servicelaag aanroepen zonder wijzigingen.

2. **ebMS kan later via een proxy**: een standalone MSH (bijv. Holodeck B2B) kan als sidecar of reverse proxy worden ingezet die ebMS3/AS4-berichten ontvangt en intern vertaalt naar REST-calls richting BBO. Dit houdt ebMS-complexiteit buiten de applicatie.

3. **Digikoppeling staat REST toe**: de Digikoppeling Architectuur stelt dat de provider bepaalt welk koppelvlak van toepassing is. BBO als provider mag REST API aanbieden.

4. **ebMS3/AS4 is nog niet breed uitgerold**: de migratie van ebMS2 naar ebMS3/AS4 bij overheidsorganisaties loopt nog. REST is voor de meeste organisaties laagdrempeliger.

### Toekomstige ebMS-ondersteuning (proxy-aanpak)

```
┌──────────────────┐     ┌──────────────────┐
│ Afnemer (REST)   │────>│                  │
└──────────────────┘     │   BBO Applicatie  │
                         │   (REST API)      │
┌──────────────────┐     │                  │
│ Afnemer (ebMS)   │──>┌─┴────────────┐     │
└──────────────────┘   │ MSH Proxy    │────>│
                       │ (Holodeck/   │     └──────────────────┘
                       │  phase4)     │
                       └──────────────┘
```

Wanneer er behoefte ontstaat aan ebMS-ondersteuning:
- Deploy een MSH als apart component (container/sidecar)
- De MSH vertaalt ebMS3/AS4-berichten naar REST-calls
- Geen wijzigingen nodig in de BBO-applicatie
- CPA-beheer en WS-Security worden afgehandeld door de MSH

## Consequenties

- **Positief**: eenvoudigere applicatie, één protocol, één contractformaat, snellere ontwikkeling
- **Positief**: ebMS-ondersteuning blijft mogelijk via proxy-aanpak zonder applicatiewijzigingen
- **Negatief**: organisaties die alleen ebMS ondersteunen kunnen niet direct aansluiten (totdat de proxy beschikbaar is)
- **Negatief**: protocol-level reliability (MSH-acknowledgments, duplicaateliminatie) moet op applicatieniveau worden opgelost (idempotency keys, Kafka)

## Referenties

- [Digikoppeling Architectuur](https://logius-standaarden.github.io/Digikoppeling-Architectuur/)
- [Digikoppeling Roadmap 2024-2025](https://gitdocumentatie.logius.nl/publicatie/dk/roadmap/2024-2025/)
- [Digikoppeling REST API Koppelvlakstandaard](https://gitdocumentatie.logius.nl/publicatie/dk/restapi/)
- [Digikoppeling ebMS2 Koppelvlakstandaard](https://gitdocumentatie.logius.nl/publicatie/dk/ebms/)
- [FBS - Hoe werkt het?](https://www.logius.nl/domeinen/interactie/federatief-berichten-stelsel/hoe-werkt-het)
