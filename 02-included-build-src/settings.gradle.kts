// TODO (scenario) Project import finishes without warning
//   Instructions:
//   - Wait for the import to finish
//   - Open the `Build` tool window
//   - Check the output for warning or error messages
//   - Check the output for deprecation warnings
// Known Issue: https://youtrack.jetbrains.com/issue/IDEA-328168/Gradle-project-import-produces-deprecation-warnings
rootProject.name = "ide-smoke-test-included-build-src"
includeBuild("build-logic")
