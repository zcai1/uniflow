package org.cfginference.core.model.slot;

import org.cfginference.core.model.location.QualifierLocation;

import javax.lang.model.type.TypeMirror;

public class PolymorphicInstanceSlot extends SourceVariableSlot {
    public PolymorphicInstanceSlot(QualifierLocation location, TypeMirror actualType) {
        super(location, actualType, false, null);
    }

    @Override
    public Kind getKind() {
        return Kind.POLYMORPHIC_INSTANCE;
    }
}
