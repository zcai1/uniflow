package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.util.serialization.Serializer;
import org.checkerframework.checker.nullness.qual.Nullable;

@AutoValue
public abstract class ExistentialSlot extends VariableSlot {
    public abstract Slot getPotentialSlot();
    public abstract Slot getAlternativeSlot();

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= getClass().hashCode();
        h *= 1000003;
        h ^= getPotentialSlot().hashCode();
        h *= 1000003;
        h ^= getAlternativeSlot().hashCode();
        return h;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof ExistentialSlot that) {
            return this.getPotentialSlot().equals(that.getPotentialSlot())
                    && this.getAlternativeSlot().equals(that.getAlternativeSlot());
        }
        return false;
    }

    @Override
    public final <S, T> S serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
