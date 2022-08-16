package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import org.cfginference.core.manager.GeneralSlotManager;
import org.cfginference.core.model.location.QualifierLocation;

@AutoValue
public abstract class RefinementSlot extends VariableSlot {
    public abstract int getRefinedSlotId();

    public final Slot getRefinedSlot(GeneralSlotManager slotManager) {
        return slotManager.getSlotById(getRefinedSlotId());
    }

    public static RefinementSlot create(int id, QualifierLocation location, int refinedSlotId) {
        return new AutoValue_RefinementSlot(id, location, refinedSlotId);
    }
}
