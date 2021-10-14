package org.gradle.example;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BuildSrcJavaPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        // TODO (scenario) Java plugin implemented in buildSrc can use another class
        // Instructions:
        // - the statement below does not show any syntax errors and cmd+click navigates to the implementation
        new JavaBuildUtils().printString("org.gradle.example.BuildSrcJavaPlugin applied on project " + target.getName());
    }
}
