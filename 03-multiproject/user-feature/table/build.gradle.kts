plugins {
    id("com.example.java-library")
}

group = "${group}.user-feature"

dependencies {
    implementation(project(":data"))

    implementation("com.example.myproduct.state:application-state")
}

// TODO (scenario) Extension from plugins applied in Groovy precompiled script plugin is available
// Instructions:
// - Verify that the `testing {}` block is not marked with a compiler error
testing {
    suites {
        val integTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":data"))
            }
        }
    }
}