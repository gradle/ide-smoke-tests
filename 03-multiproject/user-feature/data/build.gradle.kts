plugins {
    id("com.example.java-library")
}

group = "${group}.user-feature"

dependencies {
    api("com.example.myproduct.model:release")

    implementation("com.fasterxml.jackson.core:jackson-databind")
}
