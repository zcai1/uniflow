package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.qualifier.Qualifier;

@AutoValue
public abstract class ConstantSlot extends Slot {
    public abstract Qualifier getValue();

    public static ConstantSlot create(int id, Qualifier value) {
        return new AutoValue_ConstantSlot(id, value);
    }
}
