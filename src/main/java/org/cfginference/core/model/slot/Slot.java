package org.cfginference.core.model.slot;

import org.cfginference.core.annotation.VarAnnot;
import org.cfginference.core.model.qualifier.AnnotationProxy;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.util.serialization.Serializer;
import org.cfginference.core.typesystem.QualifierHierarchy;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.javacutil.AnnotationBuilder;

public abstract class Slot implements Qualifier {

    private static final String VAR_ANNOT_NAME = VarAnnot.class.getCanonicalName();

    public abstract int getId();

    // the qualifier hierarchy that owns this slot
    public abstract QualifierHierarchy getOwner();

    public abstract <S, T> S serialize(Serializer<S, T> serializer);

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= getId();
        return h;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return this == o;
    }

    @Override
    public AnnotationProxy toAnnotation() {
        return AnnotationProxy.create(
                VarAnnot.class,
                AnnotationBuilder.elementNamesValues("value", getId())
        );
    }

    @Override
    public String toString() {
        return "@%s(%s)".formatted(
                this.getClass().getSimpleName().replaceFirst("AutoValue_", ""),
                this.getId()
        );
    }
}
