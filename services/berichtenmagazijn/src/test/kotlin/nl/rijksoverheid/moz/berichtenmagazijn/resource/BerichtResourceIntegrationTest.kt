package nl.rijksoverheid.moz.berichtenmagazijn.resource

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import nl.rijksoverheid.moz.berichtenmagazijn.FbsTestResource
import nl.rijksoverheid.moz.common.FbsConstants
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.util.UUID

@QuarkusTest
@QuarkusTestResource(FbsTestResource::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class BerichtResourceIntegrationTest {

    companion object {
        private var berichtId: String? = null
    }

    @Test
    @Order(1)
    fun `POST berichten returns 201 with bericht`() {
        berichtId = given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "ontvangerIdType": "BSN",
                    "ontvangerId": "999999999",
                    "onderwerp": "Uw aanvraag is ontvangen",
                    "inhoud": "Beste inwoner, wij hebben uw aanvraag ontvangen."
                }
                """.trimIndent()
            )
            .`when`()
            .post("/api/v1/berichten")
            .then()
            .statusCode(201)
            .header("API-Version", "1.0.0")
            .body("id", notNullValue())
            .body("afzenderOin", equalTo("00000001234567890000"))
            .body("ontvangerIdType", equalTo("BSN"))
            .body("ontvangerId", equalTo("999999999"))
            .body("onderwerp", equalTo("Uw aanvraag is ontvangen"))
            .body("status", equalTo("NIEUW"))
            .extract()
            .path("id")
    }

    @Test
    @Order(2)
    fun `GET berichten returns 200 with paginated list`() {
        given()
            .queryParam("ontvangerIdType", "BSN")
            .queryParam("ontvangerId", "999999999")
            .`when`()
            .get("/api/v1/berichten")
            .then()
            .statusCode(200)
            .header("API-Version", "1.0.0")
            .body("results.size()", equalTo(1))
            .body("page", equalTo(1))
            .body("pageSize", equalTo(20))
            .body("totalElements", equalTo(1))
    }

    @Test
    @Order(3)
    fun `GET berichten by id returns 200 with bericht`() {
        given()
            .`when`()
            .get("/api/v1/berichten/$berichtId")
            .then()
            .statusCode(200)
            .header("API-Version", "1.0.0")
            .body("id", equalTo(berichtId))
            .body("status", equalTo("NIEUW"))
    }

    @Test
    @Order(4)
    fun `PATCH berichten updates status to GELEZEN`() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"status": "GELEZEN"}""")
            .`when`()
            .patch("/api/v1/berichten/$berichtId")
            .then()
            .statusCode(200)
            .header("API-Version", "1.0.0")
            .body("status", equalTo("GELEZEN"))
            .body("gelezenOp", notNullValue())
    }

    @Test
    @Order(5)
    fun `GET berichten by id returns 404 for non-existent bericht`() {
        val fakeId = UUID.randomUUID()

        given()
            .`when`()
            .get("/api/v1/berichten/$fakeId")
            .then()
            .statusCode(404)
            .header("API-Version", "1.0.0")
            .contentType(FbsConstants.MEDIA_TYPE_PROBLEM_JSON)
            .body("title", equalTo("Niet gevonden"))
            .body("status", equalTo(404))
    }

    @Test
    @Order(6)
    fun `POST berichten returns 400 for invalid input`() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "ontvangerIdType": "BSN",
                    "ontvangerId": "",
                    "onderwerp": "Test",
                    "inhoud": "Test inhoud"
                }
                """.trimIndent()
            )
            .`when`()
            .post("/api/v1/berichten")
            .then()
            .statusCode(400)
            .header("API-Version", "1.0.0")
    }

    @Test
    @Order(7)
    fun `GET bijlagen returns 200 with empty list`() {
        given()
            .`when`()
            .get("/api/v1/berichten/$berichtId/bijlagen")
            .then()
            .statusCode(200)
            .header("API-Version", "1.0.0")
            .body("size()", equalTo(0))
    }

    @Test
    @Order(8)
    fun `DELETE berichten returns 204`() {
        given()
            .`when`()
            .delete("/api/v1/berichten/$berichtId")
            .then()
            .statusCode(204)
            .header("API-Version", "1.0.0")
    }

    @Test
    @Order(9)
    fun `GET deleted bericht returns 404`() {
        given()
            .`when`()
            .get("/api/v1/berichten/$berichtId")
            .then()
            .statusCode(404)
    }
}
