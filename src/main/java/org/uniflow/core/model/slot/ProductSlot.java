package org.uniflow.core.model.slot;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.uniflow.core.annotation.ProductVar;
import org.uniflow.core.model.qualifier.AnnotationProxy;
import org.uniflow.core.model.qualifier.Qualifier;
import org.uniflow.core.typesystem.QualifierHierarchy;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.javacutil.AnnotationBuilder;

/**
 * This is simply an immutable n-tuple of {@link Slot}s, which is helpful for combining information
 * from different qualifier hierarchies.
 * <p>
 * It doesn't extend {@link Slot} so nesting {@link ProductSlot} is not possible.
 */
@AutoValue
public abstract class ProductSlot implements Qualifier {

    public abstract ImmutableMap<QualifierHierarchy, ? extends Slot> getSlots();

    @Memoized
    public ImmutableSet<Integer> getSlotIds() {
        return getSlots().values().stream().map(Slot::getId).collect(ImmutableSet.toImmutableSet());
    }

    public @Nullable Slot getSlotByHierarchy(QualifierHierarchy hierarchy) {
        return getSlots().get(hierarchy);
    }

    @Override
    @Memoized
    public AnnotationProxy toAnnotation() {
        return AnnotationProxy.create(
                ProductVar.class,
                AnnotationBuilder.elementNamesValues("slotIds", getSlotIds().asList())
        );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Slot s : getSlots().values()) {
            sb.append(s);
        }
        sb.append("}");
        return sb.toString();
    }
}
