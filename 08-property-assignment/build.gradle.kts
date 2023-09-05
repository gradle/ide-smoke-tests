// Dummy
class GreetingTask : DefaultTask() {
    // Property under test
    val greeting = project
        .objects
        .property<String>()
}

tasks {
    register("greeting", GreetingTask::class) {
        // TODO (scenario) Kotlin provider property assignment should be supported
        //   Instructions:
        //   - the statement below should not show any syntax errors
        //   - navigating to the assignment operator should navigate to the `PropertyExtensionsKt#assign` method
        greeting = "Hello, Gradle!"
    }
}

