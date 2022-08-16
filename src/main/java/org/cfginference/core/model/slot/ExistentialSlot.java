package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import org.cfginference.core.manager.GeneralSlotManager;
import org.cfginference.core.model.location.QualifierLocation;

@AutoValue
public abstract class ExistentialSlot extends VariableSlot {
    public abstract int getPotentialSlotId();
    public abstract int getAlternativeSlotId();

    public final Slot getPotentialSlot(GeneralSlotManager slotManager) {
        return slotManager.getSlotById(getPotentialSlotId());
    }

    public final Slot getAlternativeSlot(GeneralSlotManager slotManager) {
        return slotManager.getSlotById(getAlternativeSlotId());
    }

    public static ExistentialSlot create(int id,
                                         QualifierLocation location,
                                         int potentialSlotId,
                                         int alternativeSlotId) {
        return new AutoValue_ExistentialSlot(id, location, potentialSlotId, alternativeSlotId);
    }
}
