plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    // TODO (scenario) Project import finishes without warning
    //   Instructions:
    //   - Wait for the import to finish
    //   - Check whether the generated dependency accessor is detected by the IDE
    implementation(libs.commonsLang3) // Known issue: https://youtrack.jetbrains.com/issue/KTIJ-19370
                                      // Known issue: https://youtrack.jetbrains.com/issue/IDEA-283143
}
