plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.allopen) apply false
    alias(libs.plugins.quarkus) apply false
}

allprojects {
    group = "nl.fbs"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}
