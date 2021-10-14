plugins {
    id("com.example.commons")
    id("org.jetbrains.kotlin.jvm")
    id("java-library")
    id("com.example.groovy-dummy-plugin")
}

dependencies {
    implementation(kotlin("stdlib"))
}
