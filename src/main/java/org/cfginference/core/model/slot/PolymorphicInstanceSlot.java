package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.location.QualifierLocation;

@AutoValue
public abstract class PolymorphicInstanceSlot extends VariableSlot {
    public static PolymorphicInstanceSlot create(int id, QualifierLocation location) {
        return new AutoValue_PolymorphicInstanceSlot(id, location);
    }
}
