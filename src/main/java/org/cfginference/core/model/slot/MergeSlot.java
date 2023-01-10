package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.util.serialization.Serializer;
import org.checkerframework.checker.nullness.qual.Nullable;

@AutoValue
public abstract class MergeSlot extends VariableSlot {

    public abstract Slot getLeftSlot();
    public abstract Slot getRightSlot();

    // true -> lub, false -> glb
    public abstract boolean isLub();

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= getClass().hashCode();
        h *= 1000003;
        h ^= getLeftSlot().hashCode();
        h *= 1000003;
        h ^= getRightSlot().hashCode();
        h *= 1000003;
        h ^= isLub() ? 1231 : 1237;
        return h;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof MergeSlot that) {
            return this.getLeftSlot().equals(that.getLeftSlot())
                    && this.getRightSlot().equals(that.getRightSlot())
                    && this.isLub() == that.isLub();
        }
        return false;
    }

    @Override
    public final <S, T> S serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
