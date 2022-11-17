package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import org.cfginference.core.annotation.ProductVar;
import org.checkerframework.javacutil.AnnotationBuilder;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

@AutoValue
public abstract class ProductSlot extends Slot {

    private static final String PRODUCT_VAR_NAME = ProductVar.class.getCanonicalName();

    public abstract ImmutableSet<Integer> getSlotIds();

    @Override
    public AnnotationMirror toAnnotation(Elements elements) {
        return AnnotationBuilder.fromName(
                elements,
                PRODUCT_VAR_NAME,
                AnnotationBuilder.elementNamesValues("slotIds", getSlotIds().toArray())
        );
    }
}
