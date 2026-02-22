package nl.rijksoverheid.moz.berichtenlijst.resource

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Test

@QuarkusTest
class BerichtenlijstResourceIntegrationTest {

    @Test
    fun `GET berichtenlijst returns 200 with records`() {
        given()
            .queryParam("ontvangerIdType", "BSN")
            .queryParam("ontvangerId", "999999999")
            .`when`()
            .get("/api/v1/berichtenlijst")
            .then()
            .statusCode(200)
            .header("API-Version", "1.0.0")
            .body("results.size()", equalTo(2))
            .body("results[0].onderwerp", notNullValue())
            .body("results[0].magazijnUrl", notNullValue())
            .body("page", equalTo(1))
    }

    @Test
    fun `GET zoek returns filtered results`() {
        given()
            .queryParam("ontvangerIdType", "BSN")
            .queryParam("ontvangerId", "999999999")
            .queryParam("zoekterm", "belasting")
            .`when`()
            .get("/api/v1/berichtenlijst/zoek")
            .then()
            .statusCode(200)
            .header("API-Version", "1.0.0")
            .body("results.size()", equalTo(1))
            .body("results[0].onderwerp", equalTo("Belastingaanslag 2025"))
    }

    @Test
    fun `GET zoek with short zoekterm returns 400`() {
        given()
            .queryParam("ontvangerIdType", "BSN")
            .queryParam("ontvangerId", "999999999")
            .queryParam("zoekterm", "ab")
            .`when`()
            .get("/api/v1/berichtenlijst/zoek")
            .then()
            .statusCode(400)
            .header("API-Version", "1.0.0")
            .body("title", equalTo("Ongeldig verzoek"))
    }
}
