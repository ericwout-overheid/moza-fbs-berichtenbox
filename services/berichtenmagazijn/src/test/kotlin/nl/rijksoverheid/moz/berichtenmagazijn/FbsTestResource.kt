package nl.rijksoverheid.moz.berichtenmagazijn

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.GenericContainer
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class FbsTestResource : QuarkusTestResourceLifecycleManager {

    @Suppress("DEPRECATION")
    private val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16"))
        .withDatabaseName("fbs_test")
        .withUsername("test")
        .withPassword("test")

    private val minio = GenericContainer(DockerImageName.parse("minio/minio:latest"))
        .withExposedPorts(9000)
        .withEnv("MINIO_ROOT_USER", "minioadmin")
        .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
        .withCommand("server", "/data")

    private val kafka = ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))

    override fun start(): Map<String, String> {
        postgres.start()
        minio.start()
        kafka.start()

        val minioEndpoint = "http://${minio.host}:${minio.getMappedPort(9000)}"
        createMinioBucket(minioEndpoint)

        return mapOf(
            "quarkus.datasource.jdbc.url" to postgres.jdbcUrl,
            "quarkus.datasource.username" to postgres.username,
            "quarkus.datasource.password" to postgres.password,
            "kafka.bootstrap.servers" to kafka.bootstrapServers,
            "minio.endpoint" to minioEndpoint,
            "minio.access-key" to "minioadmin",
            "minio.secret-key" to "minioadmin",
            "minio.bucket" to BUCKET_NAME,
            "otel.exporter.otlp.endpoint" to "http://localhost:4317"
        )
    }

    override fun stop() {
        postgres.stop()
        minio.stop()
        kafka.stop()
    }

    private fun createMinioBucket(endpoint: String) {
        val client = MinioClient.builder()
            .endpoint(endpoint)
            .credentials("minioadmin", "minioadmin")
            .build()

        if (!client.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build())
        }
    }

    companion object {
        private const val BUCKET_NAME = "fbs-berichten"
    }
}
