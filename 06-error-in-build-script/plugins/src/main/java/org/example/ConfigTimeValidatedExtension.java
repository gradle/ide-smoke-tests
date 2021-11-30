package org.example;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class ConfigTimeValidatedExtension extends BaseExtension {

    @Inject
    public ConfigTimeValidatedExtension(ObjectFactory objectFactory) {
        super(objectFactory);
    }
}