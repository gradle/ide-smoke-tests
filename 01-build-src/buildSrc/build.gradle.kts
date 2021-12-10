plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.5.31"
    `groovy-gradle-plugin` // for precompiled Kotlin script plugins
    `kotlin-dsl` // for precompiled Kotlin script plugins
}

repositories.mavenCentral()

gradlePlugin {
    plugins {
        create("java-plugin") {
            id = "buildsrc-java-plugin"
            implementationClass = "org.gradle.example.BuildSrcJavaPlugin"
        }
        create("groovy-plugin") {
            id = "buildsrc-groovy-plugin"
            implementationClass = "org.gradle.example.BuildSrcGroovyPlugin"
        }
        create("kotlin-plugin") {
            id = "buildsrc-kotlin-plugin"
            implementationClass = "org.gradle.example.BuildSrcKotlinPlugin"
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}