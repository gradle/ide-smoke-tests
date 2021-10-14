plugins {
    id("com.example.java-library")
}

group = "${group}.user-feature"

dependencies {
    implementation(project(":data"))

    implementation("com.example.myproduct.state:application-state")
}

// TODO (scenario) extensions from plugins applied in Groovy precompiled script plugin is available
testing {
    suites {
        val integTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":data"))
            }
        }
    }
}