package nl.rijksoverheid.moz.digitalebereikbaarheid.resource

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import nl.rijksoverheid.moz.common.FbsConstants
import nl.rijksoverheid.moz.digitalebereikbaarheid.BereikbaarheidTestResource
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@QuarkusTest
@QuarkusTestResource(BereikbaarheidTestResource::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class BereikbaarheidResourceIntegrationTest {

    @Test
    @Order(1)
    fun `GET bereikbaarheid returns 404 when none exists`() {
        given()
            .queryParam("ontvangerIdType", "BSN")
            .`when`()
            .get("/api/v1/bereikbaarheid/999999999")
            .then()
            .statusCode(404)
            .header("API-Version", "1.0.0")
            .contentType(FbsConstants.MEDIA_TYPE_PROBLEM_JSON)
            .body("title", equalTo("Niet gevonden"))
            .body("status", equalTo(404))
    }

    @Test
    @Order(2)
    fun `PUT bereikbaarheid creates new registration`() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("ontvangerIdType", "BSN")
            .body(
                """
                {
                    "ontvangerId": "999999999",
                    "ontvangerIdType": "BSN",
                    "digitaalBereikbaar": true,
                    "registratieDatum": "2024-01-01T00:00:00Z"
                }
                """.trimIndent()
            )
            .`when`()
            .put("/api/v1/bereikbaarheid/999999999")
            .then()
            .statusCode(200)
            .header("API-Version", "1.0.0")
            .body("ontvangerId", equalTo("999999999"))
            .body("ontvangerIdType", equalTo("BSN"))
            .body("digitaalBereikbaar", equalTo(true))
            .body("intrekkingsDatum", nullValue())
    }

    @Test
    @Order(3)
    fun `GET bereikbaarheid returns created registration`() {
        given()
            .queryParam("ontvangerIdType", "BSN")
            .`when`()
            .get("/api/v1/bereikbaarheid/999999999")
            .then()
            .statusCode(200)
            .header("API-Version", "1.0.0")
            .body("ontvangerId", equalTo("999999999"))
            .body("digitaalBereikbaar", equalTo(true))
            .body("intrekkingsDatum", nullValue())
    }

    @Test
    @Order(4)
    fun `PUT bereikbaarheid updates to false sets intrekkingsDatum`() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("ontvangerIdType", "BSN")
            .body(
                """
                {
                    "ontvangerId": "999999999",
                    "ontvangerIdType": "BSN",
                    "digitaalBereikbaar": false,
                    "registratieDatum": "2024-01-01T00:00:00Z"
                }
                """.trimIndent()
            )
            .`when`()
            .put("/api/v1/bereikbaarheid/999999999")
            .then()
            .statusCode(200)
            .header("API-Version", "1.0.0")
            .body("digitaalBereikbaar", equalTo(false))
            .body("intrekkingsDatum", notNullValue())
    }

    @Test
    @Order(5)
    fun `PUT bereikbaarheid updates back to true clears intrekkingsDatum`() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("ontvangerIdType", "BSN")
            .body(
                """
                {
                    "ontvangerId": "999999999",
                    "ontvangerIdType": "BSN",
                    "digitaalBereikbaar": true,
                    "registratieDatum": "2024-01-01T00:00:00Z"
                }
                """.trimIndent()
            )
            .`when`()
            .put("/api/v1/bereikbaarheid/999999999")
            .then()
            .statusCode(200)
            .header("API-Version", "1.0.0")
            .body("digitaalBereikbaar", equalTo(true))
            .body("intrekkingsDatum", nullValue())
    }

    @Test
    @Order(6)
    fun `PUT bereikbaarheid returns 400 for mismatched ontvangerId`() {
        given()
            .contentType(ContentType.JSON)
            .queryParam("ontvangerIdType", "BSN")
            .body(
                """
                {
                    "ontvangerId": "111111111",
                    "ontvangerIdType": "BSN",
                    "digitaalBereikbaar": true,
                    "registratieDatum": "2024-01-01T00:00:00Z"
                }
                """.trimIndent()
            )
            .`when`()
            .put("/api/v1/bereikbaarheid/999999999")
            .then()
            .statusCode(400)
            .header("API-Version", "1.0.0")
    }
}
