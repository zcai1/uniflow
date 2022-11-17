package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ExistentialSlot extends VariableSlot {
    public abstract int getPotentialSlotId();
    public abstract int getAlternativeSlotId();
}
