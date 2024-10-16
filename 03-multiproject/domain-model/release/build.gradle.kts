// TODO (scenario) Project import finishes without warning
//   Instructions:
//   - Import this project with Java 17 (https://www.jetbrains.com/help/idea/gradle-jvm-selection.html#jvm_settings)
//     Set it in: Settings | Build, Execution, Deployment | Build Tools | Gradle
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
    myProp.set("value")
}