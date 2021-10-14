plugins {
    id("build-logic-java-plugin")
    id("build-logic-groovy-plugin")
    id("build-logic-kotlin-plugin")
}

// TODO (scenario) class from buildSrc is available in the build script
// TODO (scenario) class from buildSrc has code completion working
// TODO (scenario) can navigate to buildSrc class with cmd+click
BuildUtils.printDiagnostics("build.gradle.kts")
