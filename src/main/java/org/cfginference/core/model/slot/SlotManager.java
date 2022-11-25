package org.cfginference.core.model.slot;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.typesystem.QualifierHierarchy;
import org.cfginference.util.CollectionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO: replace Preconditions.checks with assertions once we're confident
public final class SlotManager {

    private static final ProductSlot EMPTY_PRODUCT_SLOT = new AutoValue_ProductSlot(ImmutableMap.of());

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
        Preconditions.checkArgument(0 <= slot1 && slot1 < slots.size());
        Preconditions.checkArgument(0 <= slot2 && slot2 < slots.size());

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
        Preconditions.checkArgument(0 <= srcSlot && srcSlot < slots.size());
        Preconditions.checkArgument(0 <= targetSlot && targetSlot < slots.size());

        Slot targetSlotObj = slots.get(targetSlot);
        if (targetSlotObj instanceof MergeSlot targetMergeSlot) {
            return targetMergeSlot.isLub() == isLub && isMergedInto(srcSlot, targetMergeSlot);
        }
        return false;
    }

    public @Nullable Slot getSlot(int id) {
        return 0 <= id && id < slots.size() ? slots.get(id) : null;
    }

    public ConstantSlot createConstantSlot(QualifierHierarchy owner, Qualifier value) {
        ConstantSlot slot = new AutoValue_ConstantSlot(slots.size(), owner, value);
        slots.add(slot);
        return slot;
    }

    public ArithmeticSlot createArithmeticSlot(QualifierHierarchy owner) {
        ArithmeticSlot slot = new AutoValue_ArithmeticSlot(slots.size(), owner);
        slots.add(slot);
        return slot;
    }

    public ComparisonSlot createComparisonSlot(QualifierHierarchy owner) {
        ComparisonSlot slot = new AutoValue_ComparisonSlot(slots.size(), owner);
        slots.add(slot);
        return slot;
    }

    public ExistentialSlot createExistentialSlot(QualifierHierarchy owner, int potentialSlotId, int alternativeSlotId) {
        Preconditions.checkArgument(0 <= potentialSlotId && potentialSlotId < slots.size());
        Preconditions.checkArgument(0 <= alternativeSlotId && alternativeSlotId < slots.size());

        ExistentialSlot slot = new AutoValue_ExistentialSlot(slots.size(), owner, potentialSlotId, alternativeSlotId);
        slots.add(slot);
        return slot;
    }

    public MergeSlot createMergeSlot(QualifierHierarchy owner, int leftSlotId, int rightSlotId, boolean isLub) {
        Preconditions.checkArgument(leftSlotId != rightSlotId, "Merge is redundant for %s", leftSlotId);
        Preconditions.checkArgument(0 <= leftSlotId && leftSlotId < slots.size());
        Preconditions.checkArgument(0 <= rightSlotId && rightSlotId < slots.size());

        MergeSlot slot = new AutoValue_MergeSlot(slots.size(), owner, leftSlotId, rightSlotId, isLub);
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

    public PolymorphicInstanceSlot createPolymorphicInstanceSlot(QualifierHierarchy owner) {
        PolymorphicInstanceSlot slot = new AutoValue_PolymorphicInstanceSlot(slots.size(), owner);
        slots.add(slot);
        return slot;
    }

    public RefinementSlot createRefinementSlot(QualifierHierarchy owner, int refinedSlotId) {
        Preconditions.checkArgument(0 <= refinedSlotId && refinedSlotId < slots.size());

        RefinementSlot slot = new AutoValue_RefinementSlot(slots.size(), owner, refinedSlotId);
        slots.add(slot);
        return slot;
    }

    public SourceSlot createSourceSlot(QualifierHierarchy owner, @Nullable Qualifier defaultQualifier) {
        SourceSlot slot = new AutoValue_SourceSlot(slots.size(), owner, defaultQualifier);
        slots.add(slot);
        return slot;
    }

    public ViewpointAdaptationSlot createViewpointAdaptationSlot(QualifierHierarchy owner, int receiverSlotId, int adaptingSlotId) {
        Preconditions.checkArgument(0 <= receiverSlotId && receiverSlotId < slots.size());
        Preconditions.checkArgument(0 <= adaptingSlotId && adaptingSlotId < slots.size());

        ViewpointAdaptationSlot slot = new AutoValue_ViewpointAdaptationSlot(slots.size(), owner, receiverSlotId, adaptingSlotId);
        slots.add(slot);
        return slot;
    }

    // public ProductSlot createProductSlot(Set<Integer> slotIds) {
    //     for (Integer slotId : slotIds) {
    //         Preconditions.checkArgument(0 <= slotId && slotId < slots.size());
    //     }
    //
    //     Map<QualifierHierarchy, Slot> slotMap = new LinkedHashMap<>();
    //     for (Integer slotId : slotIds) {
    //         Slot slot = slots.get(slotId);
    //         Verify.verify(
    //                 !slotMap.containsKey(slot.getQualifierHierarchy()),
    //                 "Cannot create product slot for duplicate qualifier hierarchy in %s",
    //                 slotIds
    //         );
    //         slotMap.put(slot.getQualifierHierarchy(), slot);
    //     }
    //     return new AutoValue_ProductSlot(ImmutableMap.copyOf(slotMap));
    // }

    public ProductSlot createProductSlot(Set<Slot> slots) {
        if (slots.isEmpty()) return EMPTY_PRODUCT_SLOT;

        Map<QualifierHierarchy, Slot> slotMap = new LinkedHashMap<>(slots.size());
        for (Slot slot : slots) {
            Verify.verify(
                    !slotMap.containsKey(slot.getQualifierHierarchy()),
                    "Cannot create product slot for duplicate qualifier hierarchy in %s",
                    slots
            );
            slotMap.put(slot.getQualifierHierarchy(), slot);
        }
        return new AutoValue_ProductSlot(ImmutableMap.copyOf(slotMap));
    }

    public ProductSlot createProductSlot(Map<QualifierHierarchy, Slot> slots) {
        if (slots.isEmpty()) return EMPTY_PRODUCT_SLOT;

        for (Map.Entry<QualifierHierarchy, Slot> e : slots.entrySet()) {
            Verify.verify(e.getKey() == e.getValue().getQualifierHierarchy());
        }
        return new AutoValue_ProductSlot(ImmutableMap.copyOf(slots));
    }
}
