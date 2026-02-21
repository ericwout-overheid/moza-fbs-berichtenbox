package nl.rijksoverheid.moz.notificatie.resource

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import nl.rijksoverheid.moz.common.FbsConstants
import nl.rijksoverheid.moz.notificatie.NotificatieTestResource
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.util.UUID

@QuarkusTest
@QuarkusTestResource(NotificatieTestResource::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class NotificatieResourceIntegrationTest {

    companion object {
        private var notificatieId: String? = null
    }

    @Test
    @Order(1)
    fun `POST notificaties returns 202 with notificatie`() {
        notificatieId = given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "ontvangerIdType": "BSN",
                    "ontvangerId": "999999999",
                    "kanaal": "EMAIL",
                    "onderwerp": "Test notificatie",
                    "inhoud": "U heeft een nieuw bericht ontvangen."
                }
                """.trimIndent()
            )
            .`when`()
            .post("/api/v1/notificaties")
            .then()
            .statusCode(202)
            .header("API-Version", "1.0.0")
            .body("id", notNullValue())
            .body("ontvangerIdType", equalTo("BSN"))
            .body("ontvangerId", equalTo("999999999"))
            .body("kanaal", equalTo("EMAIL"))
            .body("status", equalTo("AANGEMAAKT"))
            .extract()
            .path("id")
    }

    @Test
    @Order(2)
    fun `GET notificatie status returns 200`() {
        given()
            .`when`()
            .get("/api/v1/notificaties/$notificatieId/status")
            .then()
            .statusCode(200)
            .header("API-Version", "1.0.0")
            .body("notificatieId", equalTo(notificatieId))
            .body("status", equalTo("AANGEMAAKT"))
    }

    @Test
    @Order(3)
    fun `GET notificatie status returns 404 for non-existent`() {
        val fakeId = UUID.randomUUID()

        given()
            .`when`()
            .get("/api/v1/notificaties/$fakeId/status")
            .then()
            .statusCode(404)
            .header("API-Version", "1.0.0")
            .contentType(FbsConstants.MEDIA_TYPE_PROBLEM_JSON)
            .body("title", equalTo("Niet gevonden"))
            .body("status", equalTo(404))
    }

    @Test
    @Order(4)
    fun `POST notificaties returns 400 for invalid body`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "ontvangerIdType": "BSN",
                    "ontvangerId": "",
                    "kanaal": "EMAIL",
                    "onderwerp": "Test",
                    "inhoud": "Test"
                }
                """.trimIndent()
            )
            .`when`()
            .post("/api/v1/notificaties")
            .then()
            .statusCode(400)
            .header("API-Version", "1.0.0")
    }
}
