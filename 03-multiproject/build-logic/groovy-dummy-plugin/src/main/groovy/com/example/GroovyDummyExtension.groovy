package com.example

import org.gradle.api.provider.Property;

interface GroovyDummyExtension {
    Property<String> getMyProp()
}
