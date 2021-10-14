pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
    includeBuild("../build-logic")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}
includeBuild("../platforms")
includeBuild("../user-feature")

rootProject.name = "android-app"
include("app")
