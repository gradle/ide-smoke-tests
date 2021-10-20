// TODO (scenario) Project import finishes without warning
//   Instructions:
//   - Wait for the import to finish
//   - Open the `Build` tool window
//   - Check the output for warning or error messages
//   - Check the output for deprecation warnings

plugins {
    id("com.example.kotlin-library")
}

group = "${group}.model"

// TODO (scenario) Extension from plugin applied in Kotlin precompiled script plugin is available
//   Instructions:
//   - Verify that `groovyDummy {}` block is not marked with a compiler error and the `myProp` property can be configured in the closure
groovyDummy {
    myProp.set("value") // Known issue: https://youtrack.jetbrains.com/issue/KTIJ-19483
}