package nl.rijksoverheid.moz.berichtenmagazijn.config

import io.minio.MinioClient
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class MinioProducer(
    @param:ConfigProperty(name = "minio.endpoint") private val endpoint: String,
    @param:ConfigProperty(name = "minio.access-key") private val accessKey: String,
    @param:ConfigProperty(name = "minio.secret-key") private val secretKey: String
) {

    @Produces
    @ApplicationScoped
    fun minioClient(): MinioClient = MinioClient.builder()
        .endpoint(endpoint)
        .credentials(accessKey, secretKey)
        .build()
}
