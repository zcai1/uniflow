package org.uniflow.core.model.slot;

import com.google.auto.value.AutoValue;
import org.uniflow.core.model.util.serialization.Serializer;

@AutoValue
public abstract class RefinementSlot extends VariableSlot {
    public abstract Slot getRefinedSlot();

    @Override
    public final <S, T> S serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
