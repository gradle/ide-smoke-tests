plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(platform("com.example.platform:plugins-platform"))
    implementation(project(":commons"))
    implementation("org.springframework.boot:spring-boot-gradle-plugin")
}
