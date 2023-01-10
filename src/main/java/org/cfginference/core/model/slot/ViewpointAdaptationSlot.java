package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.util.serialization.Serializer;
import org.checkerframework.checker.nullness.qual.Nullable;

@AutoValue
public abstract class ViewpointAdaptationSlot extends VariableSlot {
    public abstract Slot getReceiverSlot();
    public abstract Slot getDeclarationSlot();

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= getClass().hashCode();
        h *= 1000003;
        h ^= getReceiverSlot().hashCode();
        h *= 1000003;
        h ^= getDeclarationSlot().hashCode();
        return h;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof ViewpointAdaptationSlot that) {
            return this.getReceiverSlot().equals(that.getReceiverSlot())
                    && this.getDeclarationSlot().equals(that.getDeclarationSlot());
        }
        return false;
    }

    @Override
    public final <S, T> S serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
