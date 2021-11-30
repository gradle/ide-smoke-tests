plugins {
    `java-gradle-plugin`
    `maven-publish`
}

group = "org.example.plugins"
version = "1.0.0"

repositories {
    mavenCentral()
}

gradlePlugin {
    val p1 by plugins.creating {
        id = "plugin.with.config.time.validation"
        implementationClass = "org.example.PluginWithConfigurationTimeValidation"
    }

    val p2 by plugins.creating {
        id = "plugin.with.exec.time.validation"
        implementationClass = "org.example.PluginWithExecutionTimeValidation"
    }
}

publishing {
    repositories {
        maven {
            url = uri(layout.projectDirectory.dir("local-repo"))
        }
    }
}
