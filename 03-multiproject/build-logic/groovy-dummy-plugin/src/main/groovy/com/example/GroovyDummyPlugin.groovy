package com.example

import org.gradle.api.Plugin
import org.gradle.api.Project

class GroovyDummyPlugin implements Plugin<Project> {
    void apply(Project target) {
        target.extensions.create("groovyDummy", GroovyDummyExtension)
        System.out.println("Hello from Groovy dummy");

        target.tasks.register("groovyDummyTask") {
            doLast {
                println "groovyDummyTask: ${project.extensions.groovyDummy.myProp.getOrNull()}"
            }
        }
     }
}
