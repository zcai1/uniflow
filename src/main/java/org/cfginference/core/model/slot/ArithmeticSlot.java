package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.location.QualifierLocation;

@AutoValue
public abstract class ArithmeticSlot extends VariableSlot {
    public static ArithmeticSlot create(int id, QualifierLocation location) {
        return new AutoValue_ArithmeticSlot(id, location);
    }
}
