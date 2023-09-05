open class PropertyTestTask : DefaultTask() {
    @Input
    val standardProperty = project
        .objects
        .property<String>()

    @InputDirectory
    val fileSystemLocationProperty = project
        .objects
        .directoryProperty()

    @Input
    val mapProperty = project
        .objects
        .mapProperty<Any, Any>()

    val setProperty = project
        .objects
        .setProperty<Any>()
}

tasks {
    // TODO (optional) Run this task to see if the property assignments work
    //  Instructions:
    //  - run the task `./gradlew propertyTest`
    register("propertyTest", PropertyTestTask::class) {
        // TODO (scenario) Kotlin single value property assignment should be supported
        //   Instructions:
        //   - the statement below should not show any syntax errors
        //   - navigating to the assignment operator should navigate to the `PropertyExtensionsKt#assign` method
        standardProperty = "Hello, Gradle!"

        // TODO (scenario) Kotlin filesystem location property assignment should be supported
        //   Instructions:
        //   - the statement below should not show any syntax errors
        //   - navigating to the assignment operator should navigate to the `PropertyExtensionsKt#assign` method
        fileSystemLocationProperty = project.layout.buildDirectory.dir("directory")

        // TODO (scenario) Kotlin set property assignment should be supported
        //   Instructions:
        //   - the statement below should not show any syntax errors
        //   - navigating to the assignment operator should navigate to the `PropertyExtensionsKt#assign` method
        setProperty = setOf("value")

        // TODO (scenario) Kotlin map property assignment should be supported
        //   Instructions:
        //   - the statement below should not show any syntax errors
        //   - navigating to the assignment operator should navigate to the `PropertyExtensionsKt#assign` method
        mapProperty = mapOf("key" to "value")
    }
}
