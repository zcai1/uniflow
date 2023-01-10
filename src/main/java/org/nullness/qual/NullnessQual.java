package org.nullness.qual;

import org.cfginference.core.model.qualifier.AnnotationProxy;
import org.cfginference.core.model.qualifier.Qualifier;

public enum NullnessQual implements Qualifier {
    NONNULL, NULLABLE;


    @Override
    public AnnotationProxy toAnnotation() {
        return AnnotationProxy.create(
                this == NullnessQual.NONNULL ? NonNull.class : Nullable.class
        );
    }
}
