package org.example;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

@SuppressWarnings("unused")
public class PluginWithConfigurationTimeValidation implements Plugin<Project> {
    public void apply(Project project) {
        project.getExtensions().create("configTimeValidatedExtension", ConfigTimeValidatedExtension.class);
        project.getGradle().projectsEvaluated( args -> project.getExtensions().getByType(ConfigTimeValidatedExtension.class).validate());
    } 
}
