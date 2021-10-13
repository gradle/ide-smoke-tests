package com.example;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JavaDummyPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        System.out.println("Hello from Java dummy");
        new HelperClass();
    }
}
