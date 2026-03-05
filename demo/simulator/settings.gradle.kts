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

rootProject.name = "fbs-demo-simulator"

includeBuild("../../") {
    dependencySubstitution {
        substitute(module("nl.fbs:fbs-client-sdk"))
            .using(project(":libs:fbs-client-sdk"))
        substitute(module("nl.fbs:fbs-common"))
            .using(project(":libs:fbs-common"))
    }
}
