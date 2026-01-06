pluginManagement {
    repositories {
        
        gradlePluginPortal()
        google()
        mavenCentral()
                maven("https://artifactory.appodeal.com/appodeal-public")
}
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        
        
maven(url = "https://jitpack.io")
                maven("https://artifactory.appodeal.com/appodeal-public")
}
}

rootProject.name = "sssazre"
include(":app")
