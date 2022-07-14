package org.nullness.qual;

import org.cfginference.core.model.qualifier.Qualifier;
import org.checkerframework.javacutil.AnnotationBuilder;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

public enum NullnessQual implements Qualifier {
    NONNULL, NULLABLE;

    @Override
    public AnnotationMirror toAnnotation(Elements elements) {
        if (this == NullnessQual.NONNULL) {
            return AnnotationBuilder.fromClass(elements, NonNull.class);
        }
        return AnnotationBuilder.fromClass(elements, Nullable.class);
    }
}
