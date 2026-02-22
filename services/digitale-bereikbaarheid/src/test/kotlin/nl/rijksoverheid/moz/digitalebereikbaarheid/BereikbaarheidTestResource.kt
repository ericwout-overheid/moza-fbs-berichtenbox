package nl.rijksoverheid.moz.digitalebereikbaarheid

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class BereikbaarheidTestResource : QuarkusTestResourceLifecycleManager {

    private val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16"))
        .withDatabaseName("fbs_test")
        .withUsername("test")
        .withPassword("test")

    override fun start(): Map<String, String> {
        postgres.start()

        return mapOf(
            "quarkus.datasource.jdbc.url" to postgres.jdbcUrl,
            "quarkus.datasource.username" to postgres.username,
            "quarkus.datasource.password" to postgres.password,
            "otel.exporter.otlp.endpoint" to "http://localhost:4317"
        )
    }

    override fun stop() {
        postgres.stop()
    }
}
