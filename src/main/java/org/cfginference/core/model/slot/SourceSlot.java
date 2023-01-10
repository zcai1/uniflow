package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.location.QualifierLocation;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.util.serialization.Serializer;
import org.checkerframework.checker.nullness.qual.Nullable;

@AutoValue
public abstract class SourceSlot extends VariableSlot {
    public abstract @Nullable ConstantSlot getDefaultQualifier();

    @Override
    public final <S, T> S serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
