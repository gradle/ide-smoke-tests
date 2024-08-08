plugins {
    `kotlin-dsl`
    alias(libs.plugins.jmh)
}

repositories {
    mavenCentral()
}

dependencies {
    // TODO (scenario) Project import finishes without warning
    //   Instructions:
    //   - Wait for the import to finish
    //   - Check whether the generated dependency accessor is detected by the IDE via hovering over the commonslang3 text
    implementation(libs.commonsLang3)
}
