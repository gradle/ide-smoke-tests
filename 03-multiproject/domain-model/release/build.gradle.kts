plugins {
    id("com.example.kotlin-library")
}

group = "${group}.model"

// TODO (scenario) extensions from plugins applied in Kotlin precompiled script plugin is available
groovyDummy {
    myProp.set("value") // Known issue: https://youtrack.jetbrains.com/issue/KTIJ-19884
}