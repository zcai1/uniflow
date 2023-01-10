package org.cfginference.core.model.constraint;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.util.serialization.Serializer;

/**
 * Represents a constraint that two slots must be comparable,  i.e. one type can be cast to the other.
 * <p>
 * In contrast, a {@link ComparisonConstraint} is created when two expressions are compared.
 */
@AutoValue
public abstract class ComparableConstraint extends Constraint {

    public abstract Slot getFirst();

    public abstract Slot getSecond();

    @Override
    @Memoized
    public ImmutableList<Slot> getSlots() {
        return ImmutableList.of(getFirst(), getSecond());
    }

    @Override
    public final <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
