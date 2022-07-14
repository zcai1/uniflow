package org.cfginference.core.model.slot;

import org.cfginference.core.model.location.QualifierLocation;
import org.cfginference.core.model.qualifier.Qualifier;

import javax.lang.model.type.TypeMirror;

public class ConstantSlot extends SourceVariableSlot {

    /**
     * The qualifier in the real type system that this slot is equivalent to
     */
    private final Qualifier value;

    public ConstantSlot(QualifierLocation location, TypeMirror actualType, boolean insertable, Qualifier defaultQualifier, Qualifier value) {
        super(location, actualType, insertable, defaultQualifier);
        this.value = value;
    }

    public Qualifier getValue() {
        return value;
    }

    @Override
    public Kind getKind() {
        return Kind.CONSTANT;
    }
}
