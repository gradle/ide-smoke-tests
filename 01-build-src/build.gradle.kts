plugins {
    // TODO (scenario) can navigate to plugin source
    `buildsrc-java-plugin` // Known issue: IDEA navigates to generated accessor and not to the declaration
    `buildsrc-groovy-plugin`
    `buildsrc-kotlin-plugin`
}

// TODO (scenario) class from buildSrc is available in the build script
// TODO (scenario) class from buildSrc has code completion working
// TODO (scenario) can navigate to buildSrc class with cmd+click
BuildUtils.printDiagnostics("build.gradle.kts")
