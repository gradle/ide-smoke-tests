plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

group = "com.example.myproduct"

android {
    compileSdk = 33
    defaultConfig {
        minSdk = 24
        targetSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(platform("com.example.platform:product-platform"))
    testImplementation(platform("com.example.platform:test-platform"))

    implementation(kotlin("stdlib"))
}
