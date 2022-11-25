package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.cfginference.core.annotation.ProductVar;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.typesystem.QualifierHierarchy;
import org.checkerframework.javacutil.AnnotationBuilder;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is simply an immutable n-tuple of {@link Slot}s, which is helpful for combining information
 * from different qualifier hierarchies.
 * <p>
 * It doesn't extend {@link Slot} so nesting {@link ProductSlot} is not possible.
 */
@AutoValue
public abstract class ProductSlot implements Qualifier {

    private static final String PRODUCT_VAR_NAME = ProductVar.class.getCanonicalName();

    public abstract ImmutableMap<QualifierHierarchy, Slot> getSlots();

    @Memoized
    public Set<Integer> getSlotIds() {
        return getSlots().values().stream().map(Slot::getId).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public AnnotationMirror toAnnotation(Elements elements) {
        return AnnotationBuilder.fromName(
                elements,
                PRODUCT_VAR_NAME,
                AnnotationBuilder.elementNamesValues("slotIds", getSlotIds().toArray())
        );
    }
}
