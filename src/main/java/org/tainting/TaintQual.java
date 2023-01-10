package org.tainting;

import org.cfginference.core.model.qualifier.AnnotationProxy;
import org.cfginference.core.model.qualifier.Qualifier;
import org.checkerframework.checker.tainting.qual.Tainted;
import org.checkerframework.checker.tainting.qual.Untainted;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

public enum TaintQual implements Qualifier {
    UNTAINTED, TAINTED;

    @Override
    public AnnotationProxy toAnnotation() {
        return AnnotationProxy.create(
                this == TAINTED ? Tainted.class : Untainted.class
        );
    }
}
