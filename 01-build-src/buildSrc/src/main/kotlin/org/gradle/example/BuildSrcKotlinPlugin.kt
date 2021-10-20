package org.gradle.example

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildSrcKotlinPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // TODO (scenario) Kotlin plugin implemented in buildSrc can use another class
        //   Instructions:
        //   - the statement below does not show any syntax errors and cmd+click navigates to the implementation
        KotlinBuildUtils().printString("org.gradle.example.BuildSrcKotlinPlugin applied on project ${target.name}")
    }
}