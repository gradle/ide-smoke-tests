package org.gradle.example;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BuildSrcJavaPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        // TODO (scenario) can use another class in plugin implementation
        new JavaBuildUtils().printString("org.gradle.example.BuildSrcJavaPlugin applied on project " + target.getName());
    }
}
