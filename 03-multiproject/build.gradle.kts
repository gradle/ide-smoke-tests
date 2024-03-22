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
//   - Navigate to server-application -> app -> Tasks -> application -> bootRun
//   - Run the :server-application:app:bootRun task
//   - Stop the running server (Click the Stop button in the run menu)
