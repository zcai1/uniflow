package org.cfginference.core.model.location;

import java.util.Objects;

public record ClassDeclLocation(String fullyQualifiedClassName) implements QualifierLocation {
    public ClassDeclLocation {
        Objects.requireNonNull(fullyQualifiedClassName);
    }

    @Override
    public String getClassName() {
        return fullyQualifiedClassName;
    }
}
