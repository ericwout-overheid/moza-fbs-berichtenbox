pluginManagement {
    val quarkusPluginVersion: String = "3.17.8"
    val kotlinVersion: String = "2.2.20"
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
        id("io.quarkus") version quarkusPluginVersion
    }
}

rootProject.name = "federatief-berichtenstelsel"

// === Libraries ===
include(":libs:fbs-common")
include(":libs:fbs-client-sdk")
include(":libs:fbs-authzen-client")
include(":libs:fbs-ldv")
include(":libs:fbs-cloudevents")

// === Services ===
include(":services:berichtenmagazijn")
include(":services:berichtenlijst")
include(":services:admin-dashboard")
