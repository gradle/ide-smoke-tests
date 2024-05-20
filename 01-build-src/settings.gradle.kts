// TODO (scenario) Project import finishes without warning
//   Instructions:
//   - Wait for the import to finish
//   - Open the `Build` tool window
//   - Check the output for warning or error messages
//   - Check the output for deprecation warnings
// Known Issue: https://youtrack.jetbrains.com/issue/IDEA-328159/kotlin-plugin-is-call-deprecated-projtect.convention-method
rootProject.name = "ide-smoke-test-build-src"

// TODO (scenario) Code completion available for settings script in project root directory
//   Instructions:
//   - Move the cursor to the last line in this file (14)
//   - Press ctrl+space (Basic code completion) and check if the content assist shows the expected options