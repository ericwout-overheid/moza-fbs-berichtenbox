plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.quarkus)
}

dependencies {
    implementation(project(":libs:fbs-common"))

    implementation(libs.quarkus.kotlin)
    implementation(libs.quarkus.rest.jackson)
    implementation(libs.quarkus.smallrye.health)
    implementation(libs.quarkus.micrometer.registry.prometheus)
    implementation(libs.jackson.module.kotlin)
    // Vaadin integratie wordt toegevoegd in Fase 6

    testImplementation(libs.quarkus.junit5)
    testImplementation(libs.mockk)
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    jvmToolchain(21)
}
