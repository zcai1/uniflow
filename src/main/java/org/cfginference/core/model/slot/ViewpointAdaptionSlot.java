package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import org.cfginference.core.manager.GeneralSlotManager;
import org.cfginference.core.model.location.QualifierLocation;

@AutoValue
public abstract class ViewpointAdaptionSlot extends VariableSlot {
    public abstract int getReceiverSlotId();
    public abstract int getAdaptingSlotId();

    public final Slot getReceiverSlot(GeneralSlotManager slotManager) {
        return slotManager.getSlotById(getReceiverSlotId());
    }

    public final Slot getAdaptingSlot(GeneralSlotManager slotManager) {
        return slotManager.getSlotById(getAdaptingSlotId());
    }

    public static ViewpointAdaptionSlot create(int id,
                                               QualifierLocation location,
                                               int receiverSlotId,
                                               int adaptingSlotId) {
        return new AutoValue_ViewpointAdaptionSlot(id, location, receiverSlotId, adaptingSlotId);
    }
}
