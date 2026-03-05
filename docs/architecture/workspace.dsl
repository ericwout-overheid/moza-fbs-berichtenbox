workspace "Federatief Berichtenstelsel" "Referentie-implementatie van het Federatief Berichtenstelsel (FBS) - BBO-opdracht Logius/BZK" {
    !docs workspace-docs

    properties {
        "nfr.betrouwbaarheid.berichtverlies" "RPO=0: geen berichtverlies; bij verstoring weigert de circuit breaker schrijfoperaties totdat duurzame persistentie is hersteld"
        "nfr.betrouwbaarheid.applicatielogging" "Bij onbeschikbaarheid logserver worden applicatie-logberichten lokaal opgeslagen voor maximaal 72 uur"
    }

    model {
        // Personen
        burger = person "Burger" "Ontvangt berichten en notificaties van overheidsorganisaties"
        medewerkerA = person "Medewerker A" "Verstuurt berichten namens Organisatie A"
        medewerkerB = person "Medewerker B" "Verstuurt berichten namens Organisatie B"
        beheerder = person "Beheerder" "Monitort en beheert het berichtenstelsel"

        // Externe systemen
        authzen = softwareSystem "AuthZEN / FTV" "Federatieve Toegangsverlening - autorisatie van verzoeken" "Extern Systeem"
        profielService = softwareSystem "Profiel Service" "Contactgegevens, communicatievoorkeuren en toestemmingsbeheer (MoZa)" "Extern Systeem"
        notificatieService = softwareSystem "Notificatie Service" "Multi-channel notificatiebezorging via e-mail, SMS en app (MoZa)" "Extern Systeem"
        kafka = softwareSystem "Kafka" "Duurzame event streaming voor bericht-lifecycle events (acks=all, 0 berichtverlies)" "Extern Systeem"

        // Deelnemende organisatie met eigen berichtenmagazijn
        orgA = softwareSystem "Organisatie A" "Deelnemende overheidsorganisatie met eigen berichtenmagazijn" "Deelnemer" {
            magazijn = container "Berichtenmagazijn" "Berichten opslaan en ophalen" "Quarkus / Kotlin" "Magazijn"
            pg = container "PostgreSQL" "Berichtmetadata" "PostgreSQL 16" "Database"
            minio = container "MinIO" "Berichtinhoud en bijlagen" "MinIO" "Database"

            magazijn -> pg "Leest/schrijft metadata" "JDBC"
            magazijn -> minio "Slaat inhoud en bijlagen op" "S3 REST API"
        }

        // Organisatie zonder eigen magazijn - gebruikt het centraal berichtenmagazijn
        orgB = softwareSystem "Organisatie B" "Deelnemende overheidsorganisatie zonder eigen magazijn" "Deelnemer"

        // Het Federatief Berichtenstelsel - een stelsel van software systemen
        group "Federatief Berichtenstelsel" {

            centraalMagazijn = softwareSystem "Centraal Berichtenmagazijn" "Berichten opslaan en ophalen voor organisaties zonder eigen magazijn" "FBS Dienst" {
                cmApp = container "Berichtenmagazijn API" "REST API voor berichten opslaan en ophalen" "Quarkus / Kotlin" "Service" {
                    cmBerichtRes = component "Berichten API" "REST endpoints voor berichten CRUD" "JAX-RS Resource"
                    cmBijlageRes = component "Bijlagen API" "REST endpoints voor bijlagen upload/download" "JAX-RS Resource"
                    cmCircuitBreaker = component "CircuitBreaker" "Weigert schrijfoperaties wanneer RPO=0 niet gegarandeerd kan worden (PostgreSQL, MinIO of Kafka onbeschikbaar)" "MicroProfile Fault Tolerance"
                    cmBerichtSvc = component "BerichtService" "Berichtlevenscyclus: aanmaken, ophalen, bijwerken, verwijderen" "CDI Bean"
                    cmAutorisatie = component "AutorisatieService" "Verifieert autorisatie via AuthZEN/FTV (fail-closed)" "CDI Bean"
                    cmEventPublisher = component "EventPublisher" "Publiceert CloudEvents NL GOV naar Kafka (gegarandeerde aflevering, 0 berichtverlies)" "Reactive Messaging"
                    cmStorageSvc = component "ObjectStorageService" "Berichtinhoud en bijlagen opslaan/ophalen" "MinIO SDK"
                    cmRepository = component "BerichtRepository" "Persistentie van berichten en bijlagen" "Panache ORM"
                    cmLdvLogger = component "LDV Logger" "Logt dataverwerkingen conform LDV-standaard" "OpenTelemetry"
                    cmAppLogger = component "Applicatie Logger" "Applicatie-logging (foutmeldingen, audit); buffert lokaal bij uitval logserver (max 72 uur)" "SLF4J / Logback"

                    cmBerichtRes -> cmCircuitBreaker "Schrijfoperaties via"
                    cmBijlageRes -> cmCircuitBreaker "Schrijfoperaties via"
                    cmCircuitBreaker -> cmBerichtSvc "Delegeert naar (als circuit closed)"
                    cmBerichtRes -> cmAutorisatie "Verifieert autorisatie"
                    cmBijlageRes -> cmAutorisatie "Verifieert autorisatie"
                    cmBerichtSvc -> cmRepository "Leest/schrijft"
                    cmBerichtSvc -> cmStorageSvc "Slaat inhoud op"
                    cmBerichtRes -> cmEventPublisher "Publiceert events"
                    cmBerichtSvc -> cmLdvLogger "Logt verwerkingen"
                    cmBerichtSvc -> cmAppLogger "Logt applicatie-events"
                    cmCircuitBreaker -> cmAppLogger "Logt circuit state changes"
                }
                cmLogBuffer = container "Lokale Log Buffer" "Lokale opslag voor applicatie-logberichten bij onbeschikbaarheid logserver (max 72 uur retentie)" "Disk" "Database"
                cmPg = container "PostgreSQL" "Berichtmetadata (transactioneel, 0 berichtverlies)" "PostgreSQL 16" "Database"
                cmMinio = container "MinIO" "Berichtinhoud en bijlagen" "MinIO" "Database"

                cmRepository -> cmPg "Leest/schrijft metadata" "JDBC"
                cmStorageSvc -> cmMinio "Slaat inhoud en bijlagen op" "S3 REST API"
                cmAppLogger -> cmLogBuffer "Buffert applicatie-logberichten lokaal bij uitval logserver" "Disk I/O"
            }

            berichtenlijst = softwareSystem "Berichtenlijst" "Aggregeert berichtrecords uit alle aangesloten magazijnen" "FBS Dienst" {
                blLogBuffer = container "Lokale Log Buffer" "Lokale opslag voor applicatie-logberichten bij onbeschikbaarheid logserver (max 72 uur retentie)" "Disk" "Database"
                blApp = container "Berichtenlijst API" "REST API voor geaggregeerde berichtrecords" "Quarkus / Kotlin" "Service" {
                    blResource = component "Berichtenlijst API" "REST endpoints voor berichtenlijst en zoeken" "JAX-RS Resource"
                    blService = component "BerichtenlijstService" "Aggregeert en cachet berichtrecords" "CDI Bean"
                    blCache = component "Cache" "In-memory cache voor berichtrecords (60s TTL)" "Caffeine"
                    blMagazijnClient = component "MagazijnClient" "REST client naar berichtenmagazijnen" "REST Client"
                    blLdvLogger = component "LDV Logger" "Logt dataverwerkingen conform LDV-standaard" "OpenTelemetry"
                    blAppLogger = component "Applicatie Logger" "Applicatie-logging (foutmeldingen, audit); buffert lokaal bij uitval logserver (max 72 uur)" "SLF4J / Logback"
                    blEventForwarder = component "EventForwarder" "Stuurt bericht-events door naar Notificatie Service" "CloudEvents / REST Client"

                    blResource -> blService "Gebruikt"
                    blService -> blCache "Leest/schrijft cache"
                    blService -> blMagazijnClient "Haalt berichtrecords op"
                    blService -> blLdvLogger "Logt verwerkingen"
                    blService -> blAppLogger "Logt applicatie-events"
                    blService -> blEventForwarder "Stuurt bericht-events door"
                }
                blAppLogger -> blLogBuffer "Buffert applicatie-logberichten lokaal bij uitval logserver" "Disk I/O"
            }

            adminDashboard = softwareSystem "Admin Dashboard" "Beheer-UI en systeemmonitoring" "FBS Dienst" {
                adLogBuffer = container "Lokale Log Buffer" "Lokale opslag voor applicatie-logberichten bij onbeschikbaarheid logserver (max 72 uur retentie)" "Disk" "Database"
                adApp = container "Admin Dashboard UI" "Web-based beheeromgeving" "Quarkus / Vaadin" "Service" {
                    adViews = component "Vaadin Views" "Dashboard, Berichten, Systeemstatus en LDV Audit Log views" "Vaadin"
                    adDataService = component "DashboardDataService" "Haalt berichtdata op via FBS Client SDK" "CDI Bean"
                    adHealthChecker = component "ServiceHealthChecker" "Controleert beschikbaarheid van FBS diensten" "HTTP Client"
                    adLdvLogger = component "LDV Logger" "Logt dataverwerkingen conform LDV-standaard" "OpenTelemetry"
                    adAppLogger = component "Applicatie Logger" "Applicatie-logging (foutmeldingen, audit); buffert lokaal bij uitval logserver (max 72 uur)" "SLF4J / Logback"

                    adViews -> adDataService "Toont data van"
                    adViews -> adHealthChecker "Toont status van"
                    adDataService -> adLdvLogger "Logt verwerkingen"
                    adDataService -> adAppLogger "Logt applicatie-events"
                }
                adAppLogger -> adLogBuffer "Buffert applicatie-logberichten lokaal bij uitval logserver" "Disk I/O"
            }

            // Gedeelde infrastructuur
            ldvLogboek = softwareSystem "LDV Logboek" "Logboek Dataverwerkingen - logging van dataverwerkingen conform LDV-standaard" "Infrastructuur"
        }

        // === Landscape relaties ===

        // Medewerkers -> hun organisatie
        medewerkerA -> magazijn "Verstuurt berichten via"
        medewerkerB -> orgB "Verstuurt berichten via"

        // Burger
        burger -> blResource "Bekijkt berichten" "REST API"

        // Beheerder
        beheerder -> adViews "Beheert systeem via" "HTTPS (browser)"

        // Organisaties -> FBS diensten
        orgB -> cmBerichtRes "Verstuurt en ontvangt berichten" "REST API via FSC"

        // Berichtenlijst notificeert externe Notificatie Service
        blEventForwarder -> notificatieService "Stuurt bericht-events door" "CloudEvents webhook" "Async"

        // Notificatie Service (extern) haalt contactgegevens op
        notificatieService -> profielService "Haalt contactgegevens en voorkeuren op" "REST API"

        // Autorisatie (component-niveau)
        cmAutorisatie -> authzen "Evalueert access request" "AuthZEN REST API"
        cmEventPublisher -> kafka "Publiceert events" "Kafka Producer" "Async"

        // Berichtenlijst -> magazijnen (component-niveau)
        blMagazijnClient -> cmApp "Haalt berichtrecords op" "REST API"
        blMagazijnClient -> orgA "Haalt berichtrecords op" "REST API via FSC"

        // Admin Dashboard -> diensten (component-niveau)
        adDataService -> cmApp "Beheert berichten" "REST API (FBS Client SDK)"
        adDataService -> blApp "Bekijkt berichtoverzichten" "REST API (FBS Client SDK)"
        adHealthChecker -> cmApp "Controleert gezondheid" "HTTP"
        adHealthChecker -> blApp "Controleert gezondheid" "HTTP"

        // LDV Logboek (component-niveau)
        cmLdvLogger -> ldvLogboek "Logt dataverwerkingen" "OTLP"
        blLdvLogger -> ldvLogboek "Logt dataverwerkingen" "OTLP"
        adLdvLogger -> ldvLogboek "Logt dataverwerkingen" "OTLP"
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

        container berichtenlijst "BerichtenlijstContainers" "Containers binnen de Berichtenlijst" {
            include *
            autoLayout
        }

        container adminDashboard "AdminDashboardContainers" "Containers binnen het Admin Dashboard" {
            include *
            autoLayout
        }

        component cmApp "BerichtenmagazijnComponenten" "Componenten binnen de Berichtenmagazijn API" {
            include *
            autoLayout
        }

        component blApp "BerichtenlijstComponenten" "Componenten binnen de Berichtenlijst API" {
            include *
            autoLayout
        }

        component adApp "AdminDashboardComponenten" "Componenten binnen de Admin Dashboard UI" {
            include *
            autoLayout
        }

        styles {
            element "Element" {
                background #1168BD
                color #ffffff
                stroke #0B4884
                fontSize 22
            }
            element "Software System" {
                background #1168BD
                color #ffffff
            }
            element "FBS Dienst" {
                background #438DD5
                color #ffffff
                stroke #2E6295
            }
            element "Deelnemer" {
                background #8C8C8C
                color #ffffff
                border dashed
            }
            element "Infrastructuur" {
                background #999999
                color #ffffff
                stroke #6B6B6B
                shape Pipe
            }
            element "Extern Systeem" {
                background #666666
                color #ffffff
                border dashed
            }
            element "Person" {
                shape Person
                background #08427B
                color #ffffff
                stroke #052E56
            }
            element "Service" {
                shape RoundedBox
                background #438DD5
                color #ffffff
                stroke #2E6295
            }
            element "Magazijn" {
                shape Hexagon
                background #438DD5
                color #ffffff
                stroke #2E6295
            }
            element "Database" {
                shape Cylinder
                background #B3B3B3
                color #000000
            }
            element "Queue" {
                shape Pipe
                background #B3B3B3
                color #000000
            }
            relationship "Relationship" {
                color #707070
                thickness 2
            }
            relationship "Async" {
                style dashed
                color #707070
            }
        }
    }

}
