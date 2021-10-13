plugins {
    `java-base`
}

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile>().configureEach {
    it.options.compilerArgs.add("-Xlint:deprecation")
}

println("Precompiled Kotlin script plugin finished")