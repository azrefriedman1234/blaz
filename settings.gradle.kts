pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // JitPack נשאר רק אם יש לך עוד ספריות משם (לא חובה ל-AAR מקומי)
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "pasiflonet_mobile"
include(":app")
