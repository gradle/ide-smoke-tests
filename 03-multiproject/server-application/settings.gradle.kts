pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("../build-logic")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
includeBuild("../platforms")
includeBuild("../user-feature")
includeBuild("../admin-feature")

rootProject.name = "server-application" // the component name
include("app")
