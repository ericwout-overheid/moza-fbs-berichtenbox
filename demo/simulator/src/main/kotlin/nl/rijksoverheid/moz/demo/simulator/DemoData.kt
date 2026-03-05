package nl.rijksoverheid.moz.demo.simulator

data class DemoOrganisatie(
    val naam: String,
    val oin: String
)

object DemoData {
    val organisaties = listOf(
        DemoOrganisatie("Belastingdienst", "00000001234567890000"),
        DemoOrganisatie("RDW", "00000009876543210000"),
        DemoOrganisatie("SVB", "00000001111111110000"),
        DemoOrganisatie("DUO", "00000002222222220000"),
        DemoOrganisatie("Gemeente Amsterdam", "00000003333333330000"),
    )

    val burgerBsns = listOf(
        "999999999", "888888888", "777777777", "666666666", "555555555",
        "444444444", "333333333", "222222222", "111111111", "123456789",
        "987654321", "112233445", "556677889", "998877665", "554433221",
        "667788990", "334455667", "778899001", "445566778", "112244668",
    )

    val onderwerpen = mapOf(
        "Belastingdienst" to listOf(
            "Aanslag inkomstenbelasting 2025",
            "Voorlopige teruggaaf 2026",
            "Herinnering aangifte inkomstenbelasting",
            "Wijziging toeslagen",
            "Bevestiging ontvangst bezwaarschrift",
        ),
        "RDW" to listOf(
            "Kentekenbewijs verlenging",
            "APK-keuring herinnering",
            "Voertuig tenaamstelling bevestiging",
            "Rijbewijsverlenging aanvraag",
            "Exportkenteken goedgekeurd",
        ),
        "SVB" to listOf(
            "AOW-uitkering toekenning",
            "Kinderbijslag wijziging",
            "Nabestaandenuitkering besluit",
            "AIO-aanvulling beschikking",
            "Overzicht jaaropgave 2025",
        ),
        "DUO" to listOf(
            "Studiefinanciering toekenning",
            "Terugbetaling studielening overzicht",
            "Diploma erkenning besluit",
            "Lerarenbeurs toekenning",
            "Inschrijving bekostiging bevestiging",
        ),
        "Gemeente Amsterdam" to listOf(
            "Verhuizing bevestiging",
            "Paspoort ophalen uitnodiging",
            "Parkeervergunning besluit",
            "Bijstandsuitkering beschikking",
            "Bouwvergunning aanvraag ontvangst",
        ),
    )

    val berichtInhouden = mapOf(
        "Belastingdienst" to "Geachte heer/mevrouw,\n\nHierbij informeren wij u over uw belastingzaken. " +
            "Neem bij vragen contact op met de BelastingTelefoon: 0800-0543.\n\nMet vriendelijke groet,\nBelastingdienst",
        "RDW" to "Geachte heer/mevrouw,\n\nHierbij informeren wij u over uw voertuigregistratie. " +
            "U kunt uw gegevens inzien via mijn.rdw.nl.\n\nMet vriendelijke groet,\nRDW",
        "SVB" to "Geachte heer/mevrouw,\n\nHierbij ontvangt u informatie over uw uitkering. " +
            "Voor vragen kunt u bellen met 0800-0227.\n\nMet vriendelijke groet,\nSociale Verzekeringsbank",
        "DUO" to "Geachte heer/mevrouw,\n\nHierbij informeren wij u over uw studiefinanciering. " +
            "Raadpleeg MijnDUO voor meer details.\n\nMet vriendelijke groet,\nDienst Uitvoering Onderwijs",
        "Gemeente Amsterdam" to "Geachte heer/mevrouw,\n\nHierbij informeren wij u over uw aanvraag bij de gemeente. " +
            "U kunt contact opnemen via 14 020.\n\nMet vriendelijke groet,\nGemeente Amsterdam",
    )
}
