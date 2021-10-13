plugins {
    `java-base`
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}

println("Precompiled Kotlin script plugin finished")