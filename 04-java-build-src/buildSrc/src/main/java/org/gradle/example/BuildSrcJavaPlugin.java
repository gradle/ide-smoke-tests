package org.gradle.example;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BuildSrcJavaPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        target.getPluginManager().apply("java-library");
        // TODO (scenario) Java plugin implemented in buildSrc can use another class if there isn't a top-level build.gradle.kts
        //   Instructions:
        //   - the statement below does not show any syntax errors and cmd+click navigates to the implementation
        new JavaBuildUtils().printString("org.gradle.example.BuildSrcJavaPlugin applied on project " + target.getName()); // Known issue: https://youtrack.jetbrains.com/issue/KTIJ-17595
    }
}