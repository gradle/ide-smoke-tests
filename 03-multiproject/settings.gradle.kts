// This is an empty umbrella build including all the component builds.
// This build is not necessarily needed. The component builds work independently.
rootProject.name = "ide-smoke-test-multiproject"

includeBuild("platforms")
includeBuild("build-logic")

includeBuild("aggregation")

includeBuild("user-feature")
includeBuild("admin-feature")

includeBuild("server-application")
includeBuild("android-app")
