// == Define locations for build logic ==
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("../build-logic")
}

// == Define locations for components ==
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
includeBuild("../platforms")
includeBuild("../user-feature")
includeBuild("../admin-feature")

// == Define the inner structure of this component ==
rootProject.name = "server-application" // the component name
include("app")
