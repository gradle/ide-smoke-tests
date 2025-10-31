import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    `java-library`
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

dependencies {
    // JUnit 4 - Required by IntelliJ Platform test framework
    testImplementation("junit:junit:4.13.2")

    // IntelliJ Platform testing
    intellijPlatform {
        create("IC", "2024.1")
        testFramework(TestFrameworkType.Platform)
        pluginVerifier()
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "IDE Smoke Tests"
    }
}

tasks.test {
    // Use Java 25 for running tests
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })

    // Configure test environment
    systemProperty("idea.test.cyclic.buffer.size", "1048576")
    jvmArgs("-Xmx2g", "-XX:+UseParallelGC")
}

tasks.register("printVersion") {
    doLast {
        println("Project version: $version")
        println("Gradle version: ${gradle.gradleVersion}")
        println("Java version: ${JavaVersion.current()}")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
