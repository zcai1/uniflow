package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ViewpointAdaptationSlot extends VariableSlot {
    public abstract int getReceiverSlotId();
    public abstract int getAdaptingSlotId();
}
