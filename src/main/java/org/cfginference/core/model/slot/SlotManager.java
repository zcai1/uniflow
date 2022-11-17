package org.cfginference.core.model.slot;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.util.CollectionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// TODO: replace Preconditions.checks with assertions once we're confident
public final class SlotManager {

    private final List<Slot> slots;

    private final SetMultimap<Integer, Integer> mergedToLubs; // TODO: merge to check

    private final SetMultimap<Integer, Integer> mergedToGlbs;

    private SlotManager(Context context) {
        slots = new ArrayList<>();
        mergedToLubs = LinkedHashMultimap.create();
        mergedToGlbs = LinkedHashMultimap.create();

        context.put(SlotManager.class, this);
    }

    public static SlotManager instance(Context context) {
        SlotManager manager = context.get(SlotManager.class);
        if (manager == null) {
            manager = new SlotManager(context);
        }
        return manager;
    }

    public @Nullable MergeSlot getMergeSlot(int slot1, int slot2, boolean isLub) {
        Preconditions.checkArgument(slot1 < slots.size());
        Preconditions.checkArgument(slot2 < slots.size());

        SetMultimap<Integer, Integer> mergedToSlots = isLub ? mergedToLubs : mergedToGlbs;
        // case 1: Merge(slot1, slot2) exists
        Set<Integer> directIntersection = CollectionUtils.intersection(mergedToSlots.get(slot1), mergedToSlots.get(slot2));
        Verify.verify(directIntersection.size() <= 1, "Duplicate merge slots for %s and %s", slot1, slot2);
        if (directIntersection.size() == 1) {
            return (MergeSlot) slots.get(directIntersection.iterator().next());
        }

        // case 2: slot1 <: slot2 and slot2 is a MergeSlot with slot2.isLub() == isLub
        if (isMergedInto(slot1, slot2, isLub)) {
            return (MergeSlot) slots.get(slot2);
        }

        // case 3: slot2 <: slot1 and slot1 is a MergeSlot with slot1.isLub() == isLub
        if (isMergedInto(slot2, slot1, isLub)) {
            return (MergeSlot) slots.get(slot1);
        }
        return null;
    }

    /**
     * @return true if {@code srcSlot} is already merged into {@code targetSlot}
     */
    private boolean isMergedInto(int srcSlot, MergeSlot targetSlot) {
        SetMultimap<Integer, Integer> mergedToMap = targetSlot.isLub() ? mergedToLubs : mergedToGlbs;
        Set<Integer> mergedToSlots = mergedToMap.get(srcSlot);
        if (mergedToSlots.contains(targetSlot.getId())) {
            return true;
        }

        for (int mergedToSlot : mergedToSlots) {
            // TODO: improve algorithm
            if (isMergedInto(mergedToSlot, targetSlot)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMergedInto(int srcSlot, int targetSlot, boolean isLub) {
        Preconditions.checkArgument(srcSlot < slots.size());
        Preconditions.checkArgument(targetSlot < slots.size());

        Slot targetSlotObj = slots.get(targetSlot);
        if (targetSlotObj instanceof MergeSlot targetMergeSlot) {
            return targetMergeSlot.isLub() == isLub && isMergedInto(srcSlot, targetMergeSlot);
        }
        return false;
    }

    public @Nullable Slot getSlot(int id) {
        return id < slots.size() ? slots.get(id) : null;
    }

    public ConstantSlot createConstantSlot(Qualifier value) {
        ConstantSlot slot = new AutoValue_ConstantSlot(slots.size(), value);
        slots.add(slot);
        return slot;
    }

    public ArithmeticSlot createArithmeticSlot() {
        ArithmeticSlot slot = new AutoValue_ArithmeticSlot(slots.size());
        slots.add(slot);
        return slot;
    }

    public ComparisonSlot createComparisonSlot() {
        ComparisonSlot slot = new AutoValue_ComparisonSlot(slots.size());
        slots.add(slot);
        return slot;
    }

    public ExistentialSlot createExistentialSlot(int potentialSlotId, int alternativeSlotId) {
        Preconditions.checkArgument(potentialSlotId < slots.size());
        Preconditions.checkArgument(alternativeSlotId < slots.size());

        ExistentialSlot slot = new AutoValue_ExistentialSlot(slots.size(), potentialSlotId, alternativeSlotId);
        slots.add(slot);
        return slot;
    }

    public MergeSlot createMergeSlot(int leftSlotId, int rightSlotId, boolean isLub) {
        Preconditions.checkArgument(leftSlotId != rightSlotId, "Merge is redundant for %s", leftSlotId);
        Preconditions.checkArgument(leftSlotId < slots.size());
        Preconditions.checkArgument(rightSlotId < slots.size());

        MergeSlot slot = new AutoValue_MergeSlot(slots.size(), leftSlotId, rightSlotId, isLub);
        slots.add(slot);
        if (isLub) {
            mergedToLubs.put(leftSlotId, slot.getId());
            mergedToLubs.put(rightSlotId, slot.getId());
        } else {
            mergedToGlbs.put(leftSlotId, slot.getId());
            mergedToGlbs.put(rightSlotId, slot.getId());
        }
        return slot;
    }

    public PolymorphicInstanceSlot createPolymorphicInstanceSlot() {
        PolymorphicInstanceSlot slot = new AutoValue_PolymorphicInstanceSlot(slots.size());
        slots.add(slot);
        return slot;
    }

    public RefinementSlot createRefinementSlot(int refinedSlotId) {
        Preconditions.checkArgument(refinedSlotId < slots.size());

        RefinementSlot slot = new AutoValue_RefinementSlot(slots.size(), refinedSlotId);
        slots.add(slot);
        return slot;
    }

    public SourceSlot createSourceSlot(@Nullable Qualifier defaultQualifier) {
        SourceSlot slot = new AutoValue_SourceSlot(slots.size(), defaultQualifier);
        slots.add(slot);
        return slot;
    }

    public ViewpointAdaptationSlot createViewpointAdaptationSlot(int receiverSlotId, int adaptingSlotId) {
        Preconditions.checkArgument(receiverSlotId < slots.size());
        Preconditions.checkArgument(adaptingSlotId < slots.size());

        ViewpointAdaptationSlot slot = new AutoValue_ViewpointAdaptationSlot(slots.size(), receiverSlotId, adaptingSlotId);
        slots.add(slot);
        return slot;
    }

    public ProductSlot createProductSlot(Iterable<Integer> slotIds) {
        for (Integer slotId : slotIds) {
            Preconditions.checkArgument(slotId < slots.size());
        }

        ProductSlot slot = new AutoValue_ProductSlot(slots.size(), ImmutableSet.copyOf(slotIds));
        slots.add(slot);
        return slot;
    }
}
