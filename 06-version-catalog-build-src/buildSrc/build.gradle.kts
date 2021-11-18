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
    implementation(libs.commonsLang3) // Known issue: IntelliJ does not detect the accessor
}
