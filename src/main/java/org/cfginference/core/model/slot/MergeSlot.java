package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import org.cfginference.core.manager.GeneralSlotManager;
import org.cfginference.core.model.location.QualifierLocation;

@AutoValue
public abstract class MergeSlot extends VariableSlot {
    public enum MergeKind {
        LUB, GLB
    }

    public abstract int getLeftSlotId();
    public abstract int getRightSlotId();
    public abstract MergeKind getMergeKind();

    public final Slot getLeftSlot(GeneralSlotManager slotManager) {
        return slotManager.getSlotById(getLeftSlotId());
    }

    public final Slot getRightSlot(GeneralSlotManager slotManager) {
        return slotManager.getSlotById(getRightSlotId());
    }

    public static MergeSlot create(int id,
                                   QualifierLocation location,
                                   int leftSlotId,
                                   int rightSlotId,
                                   MergeSlot.MergeKind mergeKind) {
        return new AutoValue_MergeSlot(id, location, leftSlotId, rightSlotId, mergeKind);
    }
}
