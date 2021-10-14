plugins {
    `groovy-gradle-plugin`
}

dependencies {
    implementation(platform("com.example.platform:plugins-platform"))

    implementation(project(":commons"))
    implementation(project(":java-dummy-plugin"))
}
