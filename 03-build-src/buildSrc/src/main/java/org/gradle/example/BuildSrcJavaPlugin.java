package org.gradle.example;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BuildSrcJavaPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        System.out.println("org.gradle.example.BuildSrcJavaPlugin applied on project " + target.getName());
    }
}
