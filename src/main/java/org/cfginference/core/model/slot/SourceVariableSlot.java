package org.cfginference.core.model.slot;

import org.cfginference.core.model.location.QualifierLocation;
import org.cfginference.core.model.qualifier.Qualifier;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.lang.model.type.TypeMirror;

/**
 * SourceVariableSlot is a VariableSlot representing a type use in the source code with undetermined value.
 */
public class SourceVariableSlot extends Slot {

    /** The actual type of the type use */
    protected final TypeMirror actualType;

    /**
     * Should this slot be inserted back into the source code.
     * This should be false for types that have an implicit annotation
     * and slots for pre-annotated code.
     */
    private boolean insertable;

    @Nullable
    private final Qualifier defaultQualifier;

    /**
     * @param location used to locate this variable in code, see @AnnotationLocation
     * @param actualType the underlying type
     * @param insertable indicates whether this slot should be inserted back into the source code
     */
    public SourceVariableSlot(QualifierLocation location, TypeMirror actualType, boolean insertable, Qualifier defaultQualifier) {
        super(location);
        this.actualType = actualType;
        this.insertable = insertable;
        this.defaultQualifier = defaultQualifier;
    }


    @Override
    public boolean isInsertable() {
        return insertable;
    }

    @Nullable
    public Qualifier getDefaultQualifier() {
        return defaultQualifier;
    }

    /**
     * Returns the underlying unannotated Java type, which this wraps.
     *
     * @return the underlying type
     */
    public TypeMirror getUnderlyingType() {
        return actualType;
    }

    @Override
    public Kind getKind() {
        return Kind.SOURCE_VARIABLE;
    }
}
