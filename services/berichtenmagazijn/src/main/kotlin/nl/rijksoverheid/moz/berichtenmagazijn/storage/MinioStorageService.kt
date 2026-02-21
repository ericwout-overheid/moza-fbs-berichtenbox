package nl.rijksoverheid.moz.berichtenmagazijn.storage

import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.io.InputStream

@ApplicationScoped
class MinioStorageService(
    private val minioClient: MinioClient,
    @param:ConfigProperty(name = "minio.bucket", defaultValue = "fbs-berichten")
    private val bucket: String
) {

    private val log = Logger.getLogger(MinioStorageService::class.java)

    fun upload(objectKey: String, inputStream: InputStream, contentType: String, size: Long) {
        log.debugf("MinIO upload: bucket=%s, objectKey=%s, size=%d", bucket, objectKey, size)
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(objectKey)
                .stream(inputStream, size, -1) // partSize -1 = use MinIO default (5 MiB)
                .contentType(contentType)
                .build()
        )
    }

    fun delete(objectKey: String) {
        log.debugf("MinIO delete: bucket=%s, objectKey=%s", bucket, objectKey)
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
