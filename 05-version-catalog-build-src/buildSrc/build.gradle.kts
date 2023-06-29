plugins {
    `kotlin-dsl`
    alias(libs.plugins.jmh) // Known issue: https://youtrack.jetbrains.com/issue/KT-49161
}

repositories {
    mavenCentral()
}

dependencies {
    // TODO (scenario) Project import finishes without warning (Known Issue) https://github.com/melix/jmh-gradle-plugin/issues/235
    //   Instructions:
    //   - Wait for the import to finish
    //   - Check whether the generated dependency accessor is detected by the IDE
    implementation(libs.commonsLang3)
}
