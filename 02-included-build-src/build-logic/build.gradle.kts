plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.5.31"
    `groovy-gradle-plugin` // for precompiled Kotlin script plugins
    `kotlin-dsl` // for precompiled Kotlin script plugins
}

repositories {
    mavenCentral()
}

group = "org.sample"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

gradlePlugin {
    plugins {
        create("java-plugin") {
            id = "build-logic-java-plugin"
            implementationClass = "org.gradle.example.BuildLogicJavaPlugin"
        }
        create("groovy-plugin") {
            id = "build-logic-groovy-plugin"
            implementationClass = "org.gradle.example.BuildLogicGroovyPlugin"
        }
        create("kotlin-plugin") {
            id = "build-logic-kotlin-plugin"
            implementationClass = "org.gradle.example.BuildLogicKotlinPlugin"
        }
    }
}