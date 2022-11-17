package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RefinementSlot extends VariableSlot {
    public abstract int getRefinedSlotId();
}
