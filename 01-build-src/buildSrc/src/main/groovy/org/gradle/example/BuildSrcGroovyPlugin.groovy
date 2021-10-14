package org.gradle.example

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildSrcGroovyPlugin implements Plugin<Project>{
    void apply(Project target) {
        // TODO (scenario) can use another class in Groovy plugin implementation
        new JavaBuildUtils().printString("org.gradle.example.BuildSrcGroovyPlugin applied on project " + target.getName());
    }
}
