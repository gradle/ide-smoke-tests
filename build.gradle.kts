import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    kotlin("jvm") version "2.2.21"
    id("org.jetbrains.intellij.platform") version "2.10.3"
}

group = "org.gradle.ide.smoke"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    // IntelliJ Platform repositories
    intellijPlatform {
        defaultRepositories()
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    testImplementation(libs.junit4)
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testRuntimeOnly(libs.junit5.launcher)
    testImplementation(libs.jackson.databind)

    // IntelliJ Platform testing
    intellijPlatform {
        create("IC", "2025.1")
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.JUnit5)
        bundledPlugin("org.jetbrains.plugins.gradle")
    }
}

tasks {
    test {
        useJUnitPlatform()

        // Configure test environment
        systemProperty("idea.test.cyclic.buffer.size", "1048576")
        jvmArgs("-Xmx2g", "-XX:+UseParallelGC")

        // Enable verbose output for tests
        testLogging {
            events("passed", "skipped", "failed", "standardOut", "standardError")
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }

        // Enable IntelliJ Platform logging
        systemProperty("idea.log.debug.categories", "#org.jetbrains.plugins.gradle")
        systemProperty("idea.log.trace.categories", "#org.jetbrains.plugins.gradle.service.project")

        // Enable Gradle daemon logging
        systemProperty("org.gradle.debug", "true")
    }
}
