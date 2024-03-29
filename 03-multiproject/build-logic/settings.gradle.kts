dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}
includeBuild("../platforms")

rootProject.name = "build-logic"
include("commons")
include("java-library")
include("java-dummy-plugin")
include("groovy-dummy-plugin")
include("kotlin-library")
include("android-application")
include("spring-boot-application")
include("report-aggregation")
