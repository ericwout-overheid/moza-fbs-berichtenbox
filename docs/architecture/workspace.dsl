workspace "Federatief Berichtenstelsel" "Referentie-implementatie van het Federatief Berichtenstelsel (FBS) - BBO-opdracht Logius/BZK" {
    !docs workspace-docs

    model {
        // Personen
        burger = person "Burger" "Ontvangt berichten en notificaties van overheidsorganisaties"
        medewerkerA = person "Medewerker A" "Verstuurt berichten namens Organisatie A"
        medewerkerB = person "Medewerker B" "Verstuurt berichten namens Organisatie B"
        medewerkerC = person "Medewerker C" "Verstuurt berichten namens Organisatie C"
        beheerder = person "Beheerder" "Monitort en beheert het berichtenstelsel"

        // Externe systemen
        authzen = softwareSystem "AuthZEN / FTV" "Federatieve Toegangsverlening - autorisatie van verzoeken" "Extern Systeem"
        profielService = softwareSystem "Profiel Service" "Contactgegevens, communicatievoorkeuren en toestemmingsbeheer (MoZa)" "Extern Systeem"
        notificatieService = softwareSystem "Notificatie Service" "Multi-channel notificatiebezorging (e-mail, SMS, app) (MoZa)" "Extern Systeem"
        smtpServer = softwareSystem "SMTP Server" "Bezorgt e-mailnotificaties" "Extern Systeem"
        smsGateway = softwareSystem "SMS Gateway" "Bezorgt SMS-notificaties" "Extern Systeem"

        // Deelnemende organisaties met eigen berichtenmagazijn
        orgA = softwareSystem "Organisatie A" "Deelnemende overheidsorganisatie met eigen berichtenmagazijn" "Deelnemer" {
            magazijnA = container "Berichtenmagazijn" "Berichten opslaan en ophalen" "Quarkus / Kotlin" "Magazijn"
            pgA = container "PostgreSQL" "Berichtmetadata" "PostgreSQL 16" "Database"
            minioA = container "MinIO" "Berichtinhoud en bijlagen" "MinIO" "Database"

            magazijnA -> pgA "Leest/schrijft metadata" "JDBC"
            magazijnA -> minioA "Slaat inhoud en bijlagen op" "S3 REST API"
        }

        orgB = softwareSystem "Organisatie B" "Deelnemende overheidsorganisatie met eigen berichtenmagazijn" "Deelnemer" {
            magazijnB = container "Berichtenmagazijn" "Berichten opslaan en ophalen" "Quarkus / Kotlin" "Magazijn"
            pgB = container "PostgreSQL" "Berichtmetadata" "PostgreSQL 16" "Database"
            minioB = container "MinIO" "Berichtinhoud en bijlagen" "MinIO" "Database"

            magazijnB -> pgB "Leest/schrijft metadata" "JDBC"
            magazijnB -> minioB "Slaat inhoud en bijlagen op" "S3 REST API"
        }

        // Organisatie zonder eigen magazijn - gebruikt het centraal berichtenmagazijn
        orgC = softwareSystem "Organisatie C" "Deelnemende overheidsorganisatie zonder eigen magazijn" "Deelnemer"

        // Het Federatief Berichtenstelsel - een stelsel van software systemen
        group "Federatief Berichtenstelsel" {

            centraalMagazijn = softwareSystem "Centraal Berichtenmagazijn" "Berichten opslaan en ophalen voor organisaties zonder eigen magazijn" "FBS Dienst" {
                cmApp = container "Berichtenmagazijn API" "REST API voor berichten opslaan en ophalen" "Quarkus / Kotlin" "Service"
                cmPg = container "PostgreSQL" "Berichtmetadata" "PostgreSQL 16" "Database"
                cmMinio = container "MinIO" "Berichtinhoud en bijlagen" "MinIO" "Database"

                cmApp -> cmPg "Leest/schrijft metadata" "JDBC"
                cmApp -> cmMinio "Slaat inhoud en bijlagen op" "S3 REST API"
            }

            berichtenlijst = softwareSystem "Berichtenlijst" "Aggregeert berichtrecords uit alle aangesloten magazijnen" "FBS Dienst" {
                blApp = container "Berichtenlijst API" "REST API voor geaggregeerde berichtrecords" "Quarkus / Kotlin" "Service"
            }

            adminDashboard = softwareSystem "Admin Dashboard" "Beheer-UI en systeemmonitoring" "FBS Dienst" {
                adApp = container "Admin Dashboard UI" "Web-based beheeromgeving" "Quarkus / Vaadin" "Service"
            }

            // Gedeelde infrastructuur
            kafka = softwareSystem "Kafka" "Asynchrone event streaming tussen magazijnen en diensten" "Infrastructuur"
            ldvLogboek = softwareSystem "LDV Logboek" "Logboek Dataverwerkingen - logging van dataverwerkingen conform LDV-standaard" "Infrastructuur"
        }

        // === Landscape relaties ===

        // Medewerkers -> hun organisatie
        medewerkerA -> orgA "Verstuurt berichten via"
        medewerkerB -> orgB "Verstuurt berichten via"
        medewerkerC -> orgC "Verstuurt berichten via"

        // Burger
        burger -> berichtenlijst "Bekijkt berichten" "REST API"

        // Beheerder
        beheerder -> adminDashboard "Beheert systeem via" "HTTPS (browser)"

        // Organisaties -> FBS diensten
        orgA -> kafka "Publiceert events (bericht-ontvangen, gelezen, verwijderd)" "FSC / Kafka"
        orgB -> kafka "Publiceert events (bericht-ontvangen, gelezen, verwijderd)" "FSC / Kafka"
        orgC -> centraalMagazijn "Verstuurt en ontvangt berichten" "REST API via FSC"

        // Berichtenlijst aggregeert uit alle magazijnen
        berichtenlijst -> centraalMagazijn "Haalt berichtrecords op" "REST API"
        berichtenlijst -> orgA "Haalt berichtrecords op" "REST API via FSC"
        berichtenlijst -> orgB "Haalt berichtrecords op" "REST API via FSC"

        // Centraal Berichtenmagazijn -> infrastructuur
        centraalMagazijn -> kafka "Publiceert events (bericht-ontvangen, gelezen, verwijderd)" "Kafka Producer"

        // Notificatie Service (extern) consumeert events en bezorgt notificaties
        notificatieService -> kafka "Consumeert bericht-ontvangen events" "Kafka Consumer"
        notificatieService -> profielService "Haalt contactgegevens en voorkeuren op" "REST API"
        notificatieService -> smtpServer "Verstuurt e-mailnotificaties" "SMTP"
        notificatieService -> smsGateway "Verstuurt SMS-notificaties" "HTTPS"

        // Admin Dashboard -> diensten
        adminDashboard -> centraalMagazijn "Beheert berichten" "REST API (FBS Client SDK)"
        adminDashboard -> berichtenlijst "Bekijkt berichtoverzichten" "REST API (FBS Client SDK)"

        // Autorisatie
        centraalMagazijn -> authzen "Verifieert autorisatie" "AuthZEN REST API"
        berichtenlijst -> authzen "Verifieert autorisatie" "AuthZEN REST API"

        // Alle FBS diensten -> LDV Logboek (via moza-logboekdataverwerking library)
        centraalMagazijn -> ldvLogboek "Logt dataverwerkingen" "LDV / ClickHouse"
        berichtenlijst -> ldvLogboek "Logt dataverwerkingen" "LDV / ClickHouse"
        adminDashboard -> ldvLogboek "Logt dataverwerkingen" "LDV / ClickHouse"
    }

    views {
        properties {
            "generatr.site.externalTag" "Extern Systeem"
            "generatr.site.nestGroups" "false"
        }

        systemLandscape "SystemLandscape" "Het Federatief Berichtenstelsel - een stelsel van federatief gekoppelde diensten" {
            include *
            autoLayout
        }

        systemContext centraalMagazijn "CentraalMagazijn" "Context van het Centraal Berichtenmagazijn" {
            include *
            autoLayout
        }

        systemContext berichtenlijst "Berichtenlijst" "Context van de Berichtenlijst" {
            include *
            autoLayout
        }

        container centraalMagazijn "CentraalMagazijnContainers" "Containers binnen het Centraal Berichtenmagazijn" {
            include *
            autoLayout
        }

        container orgA "OrganisatieA" "Berichtenmagazijn van Organisatie A" {
            include *
            autoLayout
        }

        styles {
            element "Software System" {
                background #1168bd
                color #ffffff
            }
            element "FBS Dienst" {
                background #438DD5
                color #ffffff
            }
            element "Deelnemer" {
                background #2E7D32
                color #ffffff
            }
            element "Infrastructuur" {
                background #999999
                color #ffffff
                shape Pipe
            }
            element "Extern Systeem" {
                background #666666
                color #ffffff
            }
            element "Person" {
                shape Person
                background #08427b
                color #ffffff
            }
            element "Service" {
                shape RoundedBox
                background #438DD5
                color #ffffff
            }
            element "Magazijn" {
                shape RoundedBox
                background #2E7D32
                color #ffffff
            }
            element "Database" {
                shape Cylinder
                background #999999
                color #ffffff
            }
            element "Queue" {
                shape Pipe
                background #999999
                color #ffffff
            }
        }
    }

}
