pluginManagement {
    repositories {
        
        
        
        gradlePluginPortal()
        google()
        mavenCentral()
gradlePluginPortal()
        google()
        mavenCentral()
                maven("https://artifactory.appodeal.com/appodeal-public")
}
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://artifactory.appodeal.com/appodeal-public") }
        
        


        google()
        mavenCentral()
maven(url = "https://jitpack.io")
                maven("https://artifactory.appodeal.com/appodeal-public")
}
}

rootProject.name = "sssazre"
include(":app")
