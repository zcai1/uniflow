package org.uniflow.core.model.slot;

import com.google.auto.value.AutoValue;
import org.uniflow.core.model.util.serialization.Serializer;
import org.checkerframework.checker.nullness.qual.Nullable;

@AutoValue
public abstract class SourceSlot extends VariableSlot {
    public abstract @Nullable ConstantSlot getDefaultQualifier();

    @Override
    public final <S, T> S serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
