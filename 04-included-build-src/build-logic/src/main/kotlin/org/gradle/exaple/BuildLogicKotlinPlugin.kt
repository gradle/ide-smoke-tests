package org.gradle.example

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildLogicKotlinPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        println("org.gradle.example.BuildSrcKotlinPlugin applied on project ${target.name}")
    }

}