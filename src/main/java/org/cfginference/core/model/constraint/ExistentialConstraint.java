package org.cfginference.core.model.constraint;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.util.serialization.Serializer;

@AutoValue
public abstract class ExistentialConstraint extends Constraint {

    // A variable whose annotation may or may not exist
    public abstract Slot getPotentialVariable();

    // The constraints to enforce if potentialVariable exists
    public abstract ImmutableSet<Constraint> getPotentialConstraints();

    // the constraints to enforce if potentialVariable DOES NOT exist
    public abstract ImmutableSet<Constraint> getAlternateConstraints();

    @Override
    @Memoized
    public ImmutableList<Slot> getSlots() {
        return ImmutableList.<Slot>builder()
                .add(getPotentialVariable())
                .addAll(getPotentialConstraints().stream().flatMap(c -> c.getSlots().stream()).iterator())
                .addAll(getAlternateConstraints().stream().flatMap(c -> c.getSlots().stream()).iterator())
                .build();
    }

    @Override
    public final <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
