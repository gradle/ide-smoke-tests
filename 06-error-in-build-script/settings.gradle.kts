rootProject.name = "error-in-build-script"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url = uri(file("plugins/local-repo"))
        }
    }
}
// uncomment to test local changes in the plugins
//includeBuild("plugins")
