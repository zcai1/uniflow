package org.cfginference.core.model.location;

import com.google.common.base.Preconditions;

import java.util.Objects;

public record ClassDeclLocation(String fullyQualifiedClassName) implements QualifierLocation {
    public ClassDeclLocation {
        Preconditions.checkNotNull(fullyQualifiedClassName);
    }

    @Override
    public boolean isInsertable() {
        return true;
    }
}
