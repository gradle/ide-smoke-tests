plugins {
    id("plugin.with.config.time.validation") version "1.0.0"
    id("plugin.with.exec.time.validation") version "1.0.0"
}

configTimeValidatedExtension {
    requiredProperty.set("foo") // comment this line and sync task to make validation fail
}

execTimeValidatedExtension {
   requiredProperty.set("bar") // comment this line and execute 'run' task to make validation fail
}