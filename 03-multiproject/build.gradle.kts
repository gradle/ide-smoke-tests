tasks.register("checkFeatures") {
    group = "verification"
    description = "Run all feature tests"
    dependsOn(gradle.includedBuild("admin-feature").task(":config:check"))
    dependsOn(gradle.includedBuild("user-feature").task(":data:check"))
    dependsOn(gradle.includedBuild("user-feature").task(":table:check"))
}
// TODO (scenario) Runs application with Gradle task from included build
//   Instructions:
//   - Open the `Gradle tool window`
//   - Click `Execute Gradle Task` on the toolbar
//   - Run the :server-application:app:bootRun task
//   - Stop the running server
