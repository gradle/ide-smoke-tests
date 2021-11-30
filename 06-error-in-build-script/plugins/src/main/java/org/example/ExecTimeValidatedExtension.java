package org.example;

import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class ExecTimeValidatedExtension extends BaseExtension{

    @Inject
    public ExecTimeValidatedExtension(ObjectFactory objectFactory) {
        super(objectFactory);
    }
}