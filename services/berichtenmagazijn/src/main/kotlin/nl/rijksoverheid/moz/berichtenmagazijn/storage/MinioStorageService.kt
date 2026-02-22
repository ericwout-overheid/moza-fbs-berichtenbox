package nl.rijksoverheid.moz.berichtenmagazijn.storage

import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.errors.MinioException
import jakarta.enterprise.context.ApplicationScoped
import nl.rijksoverheid.moz.berichtenmagazijn.exception.StorageException
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
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(objectKey)
                    .stream(inputStream, size, -1) // partSize -1 = MinIO SDK bepaalt automatisch
                    .contentType(contentType)
                    .build()
            )
        } catch (e: MinioException) {
            log.errorf(e, "MinIO upload mislukt: bucket=%s, objectKey=%s", bucket, objectKey)
            throw StorageException("Bijlage opslaan mislukt", e)
        }
    }

    fun delete(objectKey: String) {
        log.debugf("MinIO delete: bucket=%s, objectKey=%s", bucket, objectKey)
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(objectKey)
                    .build()
            )
        } catch (e: MinioException) {
            log.errorf(e, "MinIO delete mislukt: bucket=%s, objectKey=%s", bucket, objectKey)
            throw StorageException("Bijlage verwijderen mislukt", e)
        }
    }

    companion object {
        fun objectKey(berichtId: java.util.UUID, bijlageId: java.util.UUID, bestandsnaam: String): String {
            val safeName = bestandsnaam
                .replace(Regex("[/\\\\]"), "_")
                .replace(Regex("[^a-zA-Z0-9._\\-]"), "_")
                .take(255)
            return "berichten/$berichtId/bijlagen/$bijlageId/$safeName"
        }
    }
}
