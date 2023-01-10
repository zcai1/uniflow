package org.cfginference.core.model.constraint;

import com.google.common.collect.ImmutableList;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.util.serialization.Serializer;

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
