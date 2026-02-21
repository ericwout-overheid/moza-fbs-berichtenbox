package nl.rijksoverheid.moz.notificatieprofiel.resource

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import nl.rijksoverheid.moz.common.FbsConstants
import nl.rijksoverheid.moz.notificatieprofiel.ProfielTestResource
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@QuarkusTest
@QuarkusTestResource(ProfielTestResource::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ProfielResourceIntegrationTest {

    @Test
    @Order(1)
    fun `GET profiel returns 404 when none exists`() {
        given()
            .queryParam("ontvangerIdType", "BSN")
            .`when`()
            .get("/api/v1/profielen/999999999")
            .then()
            .statusCode(404)
            .header("API-Version", "1.0.0")
            .contentType(FbsConstants.MEDIA_TYPE_PROBLEM_JSON)
            .body("title", equalTo("Niet gevonden"))
            .body("status", equalTo(404))
    }

    @Test
    @Order(2)
    fun `PUT profiel creates new profiel (upsert)`() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("ontvangerIdType", "BSN")
            .body(
                """
                {
                    "ontvangerId": "999999999",
                    "ontvangerIdType": "BSN",
                    "emailNotificaties": true,
                    "smsNotificaties": false,
                    "emailAdres": "test@example.nl",
                    "frequentie": "DIRECT"
                }
                """.trimIndent()
            )
            .`when`()
            .put("/api/v1/profielen/999999999")
            .then()
            .statusCode(200)
            .header("API-Version", "1.0.0")
            .body("ontvangerId", equalTo("999999999"))
            .body("ontvangerIdType", equalTo("BSN"))
            .body("emailNotificaties", equalTo(true))
            .body("smsNotificaties", equalTo(false))
            .body("emailAdres", equalTo("test@example.nl"))
            .body("frequentie", equalTo("DIRECT"))
    }

    @Test
    @Order(3)
    fun `GET profiel returns created profiel`() {
        given()
            .queryParam("ontvangerIdType", "BSN")
            .`when`()
            .get("/api/v1/profielen/999999999")
            .then()
            .statusCode(200)
            .header("API-Version", "1.0.0")
            .body("ontvangerId", equalTo("999999999"))
            .body("emailNotificaties", equalTo(true))
            .body("emailAdres", equalTo("test@example.nl"))
    }

    @Test
    @Order(4)
    fun `PUT profiel updates existing profiel`() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("ontvangerIdType", "BSN")
            .body(
                """
                {
                    "ontvangerId": "999999999",
                    "ontvangerIdType": "BSN",
                    "emailNotificaties": false,
                    "smsNotificaties": true,
                    "telefoonnummer": "+31612345678",
                    "frequentie": "DAGELIJKS"
                }
                """.trimIndent()
            )
            .`when`()
            .put("/api/v1/profielen/999999999")
            .then()
            .statusCode(200)
            .header("API-Version", "1.0.0")
            .body("emailNotificaties", equalTo(false))
            .body("smsNotificaties", equalTo(true))
            .body("telefoonnummer", equalTo("+31612345678"))
            .body("frequentie", equalTo("DAGELIJKS"))
    }

    @Test
    @Order(5)
    fun `PUT profiel returns 400 for invalid body`() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("ontvangerIdType", "BSN")
            .body(
                """
                {
                    "ontvangerId": "999999999",
                    "ontvangerIdType": "BSN",
                    "emailNotificaties": true,
                    "smsNotificaties": false
                }
                """.trimIndent()
            )
            .`when`()
            .put("/api/v1/profielen/999999999")
            .then()
            .statusCode(400)
            .header("API-Version", "1.0.0")
    }

    @Test
    @Order(6)
    fun `PUT profiel returns 400 for mismatched ontvangerId`() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("ontvangerIdType", "BSN")
            .body(
                """
                {
                    "ontvangerId": "111111111",
                    "ontvangerIdType": "BSN",
                    "emailNotificaties": false,
                    "smsNotificaties": false
                }
                """.trimIndent()
            )
            .`when`()
            .put("/api/v1/profielen/999999999")
            .then()
            .statusCode(400)
            .header("API-Version", "1.0.0")
    }
}
