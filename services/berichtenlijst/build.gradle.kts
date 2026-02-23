plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.quarkus)
}

dependencies {
    implementation(project(":libs:fbs-common"))
    implementation(project(":libs:fbs-ldv"))

    implementation(libs.quarkus.kotlin)
    implementation(libs.quarkus.rest.jackson)
    implementation(libs.quarkus.rest.client.jackson)
    implementation(libs.quarkus.smallrye.openapi)
    implementation(libs.quarkus.smallrye.health)
    implementation(libs.quarkus.micrometer.registry.prometheus)
    implementation(libs.quarkus.cache)
    implementation(libs.jackson.module.kotlin)

    testImplementation(libs.quarkus.junit5)
    testImplementation(libs.rest.assured.kotlin)
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
