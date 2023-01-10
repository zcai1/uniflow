package org.cfginference.core.model.location;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ClassDeclLocation implements QualifierLocation {

    public abstract String getFullyQualifiedClassName();

    @Override
    public boolean isInsertable() {
        return true;
    }
}
