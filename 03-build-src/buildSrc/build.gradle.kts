plugins {
    `java-library`
    `groovy`
    kotlin("jvm") version "1.5.31"
    `java-gradle-plugin`
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