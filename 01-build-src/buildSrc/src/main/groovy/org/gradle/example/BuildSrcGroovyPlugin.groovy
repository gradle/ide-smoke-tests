package org.gradle.example

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildSrcGroovyPlugin implements Plugin<Project>{
    void apply(Project target) {
        System.out.println("org.gradle.example.BuildSrcGroovyPlugin applied on project " + target.getName())
    }
}
