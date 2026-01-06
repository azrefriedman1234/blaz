pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
                maven("https://artifactory.appodeal.com/appodeal-public")
}
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        
        maven(url = "https://jitpack.io")
google()
        mavenCentral()
        gradlePluginPortal()
                maven("https://artifactory.appodeal.com/appodeal-public")
}
}

rootProject.name = "sssazre"
include(":app")
