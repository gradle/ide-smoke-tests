plugins {
    id("java")
    id("com.example.jacoco")
}

group = "com.example.myproduct"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation(platform("com.example.platform:product-platform"))

    testImplementation(platform("com.example.platform:test-platform"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.test {
    useJUnitPlatform()
}
