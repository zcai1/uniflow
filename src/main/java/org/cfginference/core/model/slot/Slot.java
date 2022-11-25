package org.cfginference.core.model.slot;

import org.cfginference.core.annotation.VarAnnot;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.typesystem.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationBuilder;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

public abstract class Slot implements Qualifier {

    private static final String VAR_ANNOT_NAME = VarAnnot.class.getCanonicalName();

    public abstract int getId();

    // the owner of this slot
    public abstract QualifierHierarchy getQualifierHierarchy();

    @Override
    public AnnotationMirror toAnnotation(Elements elements) {
        return AnnotationBuilder.fromName(
                elements,
                VAR_ANNOT_NAME,
                AnnotationBuilder.elementNamesValues("value", getId())
        );
    }
}
