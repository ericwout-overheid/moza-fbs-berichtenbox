package nl.rijksoverheid.moz.berichtenmagazijn.storage

import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.io.InputStream

@ApplicationScoped
class MinioStorageService(
    private val minioClient: MinioClient,
    @param:ConfigProperty(name = "minio.bucket", defaultValue = "fbs-berichten")
    private val bucket: String
) {

    fun upload(objectKey: String, inputStream: InputStream, contentType: String, size: Long) {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(objectKey)
                .stream(inputStream, size, -1)
                .contentType(contentType)
                .build()
        )
    }

    fun delete(objectKey: String) {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucket)
                .`object`(objectKey)
                .build()
        )
    }

    companion object {
        fun objectKey(berichtId: java.util.UUID, bijlageId: java.util.UUID, bestandsnaam: String): String =
            "berichten/$berichtId/bijlagen/$bijlageId/$bestandsnaam"
    }
}
