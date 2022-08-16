package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.location.QualifierLocation;

@AutoValue
public abstract class ComparisonSlot extends VariableSlot {
    public static ComparisonSlot create(int id, QualifierLocation location) {
        return new AutoValue_ComparisonSlot(id, location);
    }
}
