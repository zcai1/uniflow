package org.uniflow.core.model.constraint;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import org.uniflow.core.model.slot.Slot;
import org.uniflow.core.model.util.serialization.Serializer;

import java.util.Set;

@AutoValue
public abstract class ImplicationConstraint extends Constraint {

    public abstract Set<Constraint> getAssumptions();

    public abstract Constraint getConclusion();

    @Override
    @Memoized
    public ImmutableList<Slot> getSlots() {
        return ImmutableList.<Slot>builder()
                .addAll(getAssumptions().stream().flatMap(c -> c.getSlots().stream()).iterator())
                .addAll(getConclusion().getSlots())
                .build();
    }

    @Override
    public final <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
