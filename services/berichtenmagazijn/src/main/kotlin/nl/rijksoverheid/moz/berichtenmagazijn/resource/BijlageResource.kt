package nl.rijksoverheid.moz.berichtenmagazijn.resource

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import nl.rijksoverheid.moz.berichtenmagazijn.service.BerichtService
import org.jboss.resteasy.reactive.multipart.FileUpload
import org.jboss.resteasy.reactive.RestForm
import java.util.UUID

@Path("/api/v1/berichten/{berichtId}/bijlagen")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
class BijlageResource(
    private val berichtService: BerichtService
) {

    @GET
    fun lijstBijlagen(@PathParam("berichtId") berichtId: UUID): Response {
        val bijlagen = berichtService.lijstBijlagen(berichtId)
        return Response.ok(bijlagen).build()
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun uploadBijlage(
        @PathParam("berichtId") berichtId: UUID,
        @RestForm("bestand") bestand: FileUpload
    ): Response {
        val bijlage = berichtService.uploadBijlage(
            berichtId = berichtId,
            bestandsnaam = bestand.fileName(),
            mediaType = bestand.contentType(),
            inputStream = bestand.filePath().toFile().inputStream(),
            grootte = bestand.size()
        )
        return Response.status(Response.Status.CREATED).entity(bijlage).build()
    }
}
