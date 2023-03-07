package org.uniflow.core.model.constraint;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import org.uniflow.core.model.slot.Slot;
import org.uniflow.core.model.util.serialization.Serializer;

@AutoValue
public abstract class SubtypeConstraint extends Constraint {

    public abstract Slot getSubtype();

    public abstract Slot getSupertype();

    @Override
    @Memoized
    public ImmutableList<Slot> getSlots() {
        return ImmutableList.of(getSubtype(), getSupertype());
    }

    @Override
    public final <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
