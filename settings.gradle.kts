pluginManagement {
    repositories {
        google()
        // Google's Maven Central mirror: repo.maven.apache.org is rate-limiting CI runs.
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        mavenCentral()
    }
}

rootProject.name = "klokk"

include(":composeApp")
include(":androidApp")
