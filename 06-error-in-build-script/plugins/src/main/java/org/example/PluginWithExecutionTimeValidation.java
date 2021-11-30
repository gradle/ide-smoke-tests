package org.example;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

@SuppressWarnings("unused")
public class PluginWithExecutionTimeValidation implements Plugin<Project> {
    public void apply(Project project) {
        project.getExtensions().create("execTimeValidatedExtension", ExecTimeValidatedExtension.class);
        TaskProvider<Task> validateTask = project.getTasks().register("validate", task -> {
            configureTaskGroup(task);
            task.doLast(ignore -> project.getExtensions().getByType(ExecTimeValidatedExtension.class).validate());
        });
        project.getTasks().register("run", task -> {
            configureTaskGroup(task);
            task.dependsOn(validateTask);
            task.doLast(ignore -> System.out.println("Run task executed"));
        });
    }

    private static void configureTaskGroup(Task task) {
        task.setGroup("smoke test");
    }
}
