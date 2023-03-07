package org.uniflow.core.model.constraint;

import com.google.common.collect.ImmutableList;
import org.uniflow.core.model.slot.Slot;
import org.uniflow.core.model.util.serialization.Serializer;

public class ConstantConstraint extends Constraint {
    @Override
    public ImmutableList<Slot> getSlots() {
        return ImmutableList.of();
    }

    @Override
    public <S, T> T serialize(Serializer<S, T> serializer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
