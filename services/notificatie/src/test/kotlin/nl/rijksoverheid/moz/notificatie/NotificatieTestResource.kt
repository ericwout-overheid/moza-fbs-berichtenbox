package nl.rijksoverheid.moz.notificatie

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class NotificatieTestResource : QuarkusTestResourceLifecycleManager {

    private val postgres = PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres:16"))
        .withDatabaseName("fbs_test")
        .withUsername("test")
        .withPassword("test")

    private val kafka = ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"))

    override fun start(): Map<String, String> {
        postgres.start()
        kafka.start()

        return mapOf(
            "quarkus.datasource.jdbc.url" to postgres.jdbcUrl,
            "quarkus.datasource.username" to postgres.username,
            "quarkus.datasource.password" to postgres.password,
            "kafka.bootstrap.servers" to kafka.bootstrapServers,
            "otel.exporter.otlp.endpoint" to "http://localhost:4317",
            "quarkus.rest-client.notificatieprofiel-api.url" to "http://localhost:19999",
            "quarkus.mailer.mock" to "true"
        )
    }

    override fun stop() {
        postgres.stop()
        kafka.stop()
    }
}
