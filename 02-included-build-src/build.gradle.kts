plugins {
    id("build-logic-java-plugin")
    id("build-logic-groovy-plugin")
    id("build-logic-kotlin-plugin")
    id("conventions-in-groovy-dsl")
    id("conventions-in-kotlin-dsl")
}

BuildUtils.printDiagnostics("build.gradle.kts")
