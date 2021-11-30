package org.example;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class BaseExtension {
    private final Property<String> requiredProperty;

    public BaseExtension(ObjectFactory objectFactory) {
        requiredProperty = objectFactory.property(String.class);
    }

    @SuppressWarnings("unused")
    public Property<String> getRequiredProperty() {
        return requiredProperty;
    }

    void validate() {
        if (!requiredProperty.isPresent()) {
            throw new RuntimeException("Validation failed: 'requiredProperty' must be set");
        }
    }
}
