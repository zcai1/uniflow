package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class MergeSlot extends VariableSlot {
    public abstract int getLeftSlotId();
    public abstract int getRightSlotId();
    // true -> lub, false -> glb
    public abstract boolean isLub();
}
