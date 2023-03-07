package org.tainting;

import org.uniflow.core.model.qualifier.AnnotationProxy;
import org.uniflow.core.model.qualifier.Qualifier;
import org.checkerframework.checker.tainting.qual.Tainted;
import org.checkerframework.checker.tainting.qual.Untainted;

public enum TaintQual implements Qualifier {
    UNTAINTED, TAINTED;

    @Override
    public AnnotationProxy toAnnotation() {
        return AnnotationProxy.create(
                this == TAINTED ? Tainted.class : Untainted.class
        );
    }
}
