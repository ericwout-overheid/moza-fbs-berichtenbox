plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.quarkus)
}

dependencies {
    implementation(project(":libs:fbs-common"))
    implementation(project(":libs:fbs-client-sdk"))
    implementation(project(":libs:fbs-ldv"))

    // Vaadin
    implementation(enforcedPlatform(libs.vaadin.bom))
    implementation(libs.vaadin.core)
    implementation(libs.vaadin.quarkus)

    // Quarkus
    implementation(libs.quarkus.kotlin)
    implementation(libs.quarkus.rest.jackson)
    implementation(libs.quarkus.smallrye.health)
    implementation(libs.quarkus.micrometer.registry.prometheus)
    implementation(libs.jackson.module.kotlin)

    testImplementation(libs.quarkus.junit5)
    testImplementation(libs.mockk)
    testImplementation(kotlin("test"))
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("com.vaadin.flow.router.Route")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    jvmToolchain(21)
}
