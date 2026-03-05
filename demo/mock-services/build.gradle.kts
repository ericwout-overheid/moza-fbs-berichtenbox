plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("io.quarkus")
}

group = "nl.rijksoverheid.moz.demo"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val quarkusVersion = "3.17.8"
val jacksonVersion = "2.21.0"
val cloudeventsVersion = "4.0.1"

dependencies {
    implementation("nl.fbs:fbs-common")
    implementation("nl.fbs:fbs-cloudevents")

    implementation(platform("io.quarkus.platform:quarkus-bom:$quarkusVersion"))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-messaging-kafka")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("io.cloudevents:cloudevents-kafka:$cloudeventsVersion")
    implementation("io.cloudevents:cloudevents-json-jackson:$cloudeventsVersion")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation(kotlin("test"))
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
