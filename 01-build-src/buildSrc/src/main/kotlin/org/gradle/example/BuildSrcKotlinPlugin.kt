package org.gradle.example

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildSrcKotlinPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // TODO (scenario) can use another class in Kotlin plugin implementation
        KotlinBuildUtils().printString("org.gradle.example.BuildSrcKotlinPlugin applied on project ${target.name}")
    }
}