package org.gradle.example

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildSrcGroovyPlugin implements Plugin<Project> { // Known issue: https://youtrack.jetbrains.com/issue/IDEA-280465
    void apply(Project target) {
        // TODO (scenario) Groovy plugin implemented in buildSrc can use another class
        //   Instructions:
        //   - the statements below does not show any syntax errors
        //   - cmd+click navigates to the implementation
        new JavaBuildUtils().printString("org.gradle.example.BuildSrcGroovyPlugin applied on project " + target.getName())
        new GroovyBuildUtils().printString("org.gradle.example.BuildSrcGroovyPlugin applied on project " + target.getName())
    }
}
