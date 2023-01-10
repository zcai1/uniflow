package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.location.QualifierLocation;
import org.cfginference.core.model.util.serialization.Serializer;

// NOTE: A poly instance is not a poly qualifier.
@AutoValue
public abstract class PolymorphicInstanceSlot extends VariableSlot {

    @Override
    public final <S, T> S serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
