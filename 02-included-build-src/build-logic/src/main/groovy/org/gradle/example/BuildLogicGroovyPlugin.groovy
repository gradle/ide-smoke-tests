package org.gradle.example

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildLogicGroovyPlugin implements Plugin<Project>{
    void apply(Project target) {
        // TODO (scenario) Groovy plugin implemented in included build can use another class
        //   Instructions:
        //   - the statement below does not show any syntax errors and cmd+click navigates to the implementation
        new JavaBuildUtils().printString("org.gradle.example.BuildSrcGroovyPlugin applied on project " + target.getName());
    }
}
