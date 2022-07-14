package org.cfginference.core.model.slot;

import org.cfginference.core.model.location.QualifierLocation;

public class MergeSlot extends Slot {

    private final Slot left;
    private final Slot right;
    private final MergeKind mergeKind;

    public MergeSlot(QualifierLocation location, Slot left, Slot right, MergeKind mergeKind) {
        super(location);
        this.left = left;
        this.right = right;
        this.mergeKind = mergeKind;
    }

    public Slot getLeft() {
        return left;
    }

    public Slot getRight() {
        return right;
    }

    @Override
    public Slot.Kind getKind() {
        return Kind.MERGE;
    }

    public MergeKind getMergeKind() {
        return mergeKind;
    }

    @Override
    public boolean isInsertable() {
        return false;
    }

    public enum MergeKind {
        LUB, GLB
    }
}
