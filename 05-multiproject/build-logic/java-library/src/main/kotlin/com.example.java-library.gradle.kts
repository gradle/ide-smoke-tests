plugins {
    id("com.example.commons")
    id("java-library")
    id("com.example.java-dummy-plugin")
}

testing {
    suites {
        register<JvmTestSuite>("integTest") {
            useJUnitJupiter()
            dependencies {
                implementation(project)
            }
        }
    }
}
