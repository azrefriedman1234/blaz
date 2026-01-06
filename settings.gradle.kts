pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven { url = uri("https://maven-central.storage-download.googleapis.com/maven2/") }
        maven { url = uri("https://repo1.maven.org/maven2/") }
        mavenCentral()
        maven { url = uri("https://artifactory.appodeal.com/appodeal-public") }

        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {

        // fallback if Maven Central endpoint returns 403 in CI:
        maven { url = uri("https://repo1.maven.org/maven2") }
    }
}

rootProject.name = "sssazre"
include(":app")
