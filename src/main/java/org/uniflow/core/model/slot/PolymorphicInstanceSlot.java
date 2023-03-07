package org.uniflow.core.model.slot;

import com.google.auto.value.AutoValue;
import org.uniflow.core.model.util.serialization.Serializer;

// NOTE: A poly instance is not a poly qualifier.
@AutoValue
public abstract class PolymorphicInstanceSlot extends VariableSlot {

    @Override
    public final <S, T> S serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
