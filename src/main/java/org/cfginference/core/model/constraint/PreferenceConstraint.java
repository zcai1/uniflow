package org.cfginference.core.model.constraint;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.slot.VariableSlot;
import org.cfginference.core.model.util.serialization.Serializer;

@AutoValue
public abstract class PreferenceConstraint extends Constraint {

    public abstract VariableSlot getVariable();

    public abstract ConstantSlot getGoal();

    public abstract int getWeight();

    @Override
    @Memoized
    public ImmutableList<Slot> getSlots() {
        return ImmutableList.of(getVariable(), getGoal());
    }

    @Override
    public final <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
