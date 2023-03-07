package org.uniflow.core.model.slot;

import com.google.auto.value.AutoValue;
import org.uniflow.core.model.qualifier.Qualifier;
import org.uniflow.core.model.util.serialization.Serializer;
import org.checkerframework.checker.nullness.qual.Nullable;

@AutoValue
public abstract class ConstantSlot extends Slot {

    public abstract Qualifier getValue();

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= getClass().hashCode();
        h *= 1000003;
        h ^= getValue().hashCode();
        return h;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ConstantSlot that) {
            return getValue().equals(that.getValue());
        }
        return false;
    }

    @Override
    public final <S, T> S serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }

    @Override
    public String toString() {
        return super.toString() + " " + getValue();
    }
}
