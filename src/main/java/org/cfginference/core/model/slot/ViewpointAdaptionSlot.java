package org.cfginference.core.model.slot;

import org.cfginference.core.model.location.QualifierLocation;

public class ViewpointAdaptionSlot extends Slot {
    private final Slot receiverSlot;
    private final Slot adaptingSlot;

    public ViewpointAdaptionSlot(QualifierLocation location, Slot receiverSlot, Slot adaptingSlot) {
        super(location);
        this.receiverSlot = receiverSlot;
        this.adaptingSlot = adaptingSlot;
    }

    public Slot getReceiverSlot() {
        return receiverSlot;
    }

    public Slot getAdaptingSlot() {
        return adaptingSlot;
    }

    @Override
    public boolean isInsertable() {
        return false;
    }

    @Override
    public Kind getKind() {
        return Kind.VIEWPOINT_ADAPTION;
    }
}
