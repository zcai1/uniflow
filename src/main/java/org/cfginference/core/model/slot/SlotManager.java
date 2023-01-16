package org.cfginference.core.model.slot;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.PluginOptions;
import org.cfginference.core.model.location.QualifierLocation;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.util.SlotLocator;
import org.cfginference.core.typesystem.QualifierHierarchy;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.javacutil.CollectionUtils;
import org.plumelib.util.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// TODO: replace Preconditions.checks with assertions once we're confident
public final class SlotManager {

    private static final ProductSlot EMPTY_PRODUCT_SLOT = new AutoValue_ProductSlot(ImmutableMap.of());

    private final List<Slot> slots;

    private final SlotLocator slotLocator;

    private final Map<Qualifier, ConstantSlot> constantSlots;

    private final Map<Pair<Slot, Slot>, ExistentialSlot> existentialSlots;

    private final Map<Pair<Slot, Slot>, ViewpointAdaptationSlot> viewpointAdaptationSlots;

    private final Map<Pair<Slot, Slot>, MergeSlot> lubSlots;

    private final Map<Pair<Slot, Slot>, MergeSlot> glbSlots;

    private final Map<Set<Integer>, ProductSlot> productSlotsCache;

    private final SetMultimap<Integer, Integer> mergedToLubs; // TODO: merge to check

    private final SetMultimap<Integer, Integer> mergedToGlbs;

    private SlotManager(Context context) {
        PluginOptions options = PluginOptions.instance(context);

        slots = new ArrayList<>();
        slotLocator = SlotLocator.instance(context);
        constantSlots = new LinkedHashMap<>();
        existentialSlots = new LinkedHashMap<>();
        viewpointAdaptationSlots = new LinkedHashMap<>();
        lubSlots = new LinkedHashMap<>();
        glbSlots = new LinkedHashMap<>();
        productSlotsCache = CollectionUtils.createLRUCache(options.getCacheSize());
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

    public List<Slot> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    public @Nullable MergeSlot getExistingMergeSlot(Slot slot1, Slot slot2, boolean isLub) {
        Map<Pair<Slot, Slot>, MergeSlot> cache = isLub ? lubSlots : glbSlots;
        // case 1: Merge(slot1, slot2) exists
        MergeSlot directMerge = cache.get(Pair.of(slot1, slot2));
        if (directMerge != null) {
            return directMerge;
        }

        // case 2: slot1 is already merged into slot2
        if (isMergedInto(slot1, slot2, isLub)) {
            return (MergeSlot) slot2;
        }

        // case 3: slot2 is already merged into slot1
        if (isMergedInto(slot2, slot1, isLub)) {
            return (MergeSlot) slot1;
        }
        return null;
    }

    /**
     * @return true if {@code srcSlot} is already merged into {@code targetSlot}
     */
    private boolean isMergedInto(int srcSlot, MergeSlot targetSlot) {
        SetMultimap<Integer, Integer> mergedToMap = targetSlot.isLub() ? mergedToLubs : mergedToGlbs;
        Deque<Integer> queue = new ArrayDeque<>();

        queue.add(srcSlot);
        while (!queue.isEmpty()) {
            Integer nextSlot = queue.remove();
            Set<Integer> mergedToSlots = mergedToMap.get(nextSlot);

            if (mergedToSlots.contains(targetSlot.getId())) {
                return true;
            } else {
                queue.addAll(mergedToSlots);
            }
        }
        return false;
    }

    public boolean isMergedInto(Slot srcSlot, MergeSlot targetSlot) {
        return isMergedInto(srcSlot.getId(), targetSlot);
    }

    public boolean isMergedInto(Slot srcSlot, Slot targetSlot, boolean isLub) {
        if (targetSlot instanceof MergeSlot targetMergeSlot) {
            return targetMergeSlot.isLub() == isLub && isMergedInto(srcSlot, targetMergeSlot);
        }
        return false;
    }

    public @Nullable Slot getSlot(int id) {
        return 0 <= id && id < slots.size() ? slots.get(id) : null;
    }

    public ConstantSlot createConstantSlot(QualifierHierarchy owner, Qualifier value) {
        assert owner.isInHierarchy(value);
        Preconditions.checkArgument(!(value instanceof Slot));

        ConstantSlot slot = constantSlots.computeIfAbsent(value,
                k -> new AutoValue_ConstantSlot(slots.size(), owner, value));

        if (slot.getId() == slots.size()) {
            slots.add(slot);
        }
        return slot;
    }

    public ArithmeticSlot createArithmeticSlot(QualifierHierarchy owner, QualifierLocation location) {
        Preconditions.checkNotNull(location);

        ArithmeticSlot slot = new AutoValue_ArithmeticSlot(slots.size(), owner);
        slots.add(slot);
        slotLocator.addLocation(slot, location);
        return slot;
    }

    public ComparisonSlot createComparisonSlot(QualifierHierarchy owner, QualifierLocation location) {
        Preconditions.checkNotNull(location);

        ComparisonSlot slot = new AutoValue_ComparisonSlot(slots.size(), owner);
        slots.add(slot);
        slotLocator.addLocation(slot, location);
        return slot;
    }

    public ExistentialSlot createExistentialSlot(QualifierHierarchy owner, QualifierLocation location, Slot potentialSlot, Slot alternativeSlot) {
        Preconditions.checkNotNull(location);

        ExistentialSlot slot = existentialSlots.computeIfAbsent(Pair.of(potentialSlot, alternativeSlot),
                k -> new AutoValue_ExistentialSlot(slots.size(), owner, potentialSlot, alternativeSlot));

        if (slot.getId() == slots.size()) {
            slots.add(slot);
            slotLocator.addLocation(slot, location);
        }
        return slot;
    }

    public MergeSlot createMergeSlot(QualifierHierarchy owner,
                                     QualifierLocation location,
                                     Slot leftSlot,
                                     Slot rightSlot,
                                     boolean isLub) {
        Preconditions.checkNotNull(location);
        Preconditions.checkArgument(!leftSlot.equals(rightSlot), "Merge is redundant for %s", leftSlot);

        Map<Pair<Slot, Slot>, MergeSlot> cache = isLub ? lubSlots : glbSlots;
        MergeSlot slot = cache.computeIfAbsent(Pair.of(leftSlot, rightSlot),
                k -> new AutoValue_MergeSlot(slots.size(), owner, leftSlot, rightSlot, isLub));

        if (slot.getId() == slots.size()) {
            slots.add(slot);
            slotLocator.addLocation(slot, location);
        }
        if (isLub) {
            mergedToLubs.put(leftSlot.getId(), slot.getId());
            mergedToLubs.put(rightSlot.getId(), slot.getId());
        } else {
            mergedToGlbs.put(leftSlot.getId(), slot.getId());
            mergedToGlbs.put(rightSlot.getId(), slot.getId());
        }
        return slot;
    }

    public PolymorphicInstanceSlot createPolymorphicInstanceSlot(QualifierHierarchy owner, QualifierLocation location) {
        Preconditions.checkNotNull(location);

        PolymorphicInstanceSlot slot = new AutoValue_PolymorphicInstanceSlot(slots.size(), owner);
        slots.add(slot);
        slotLocator.addLocation(slot, location);
        return slot;
    }

    public RefinementSlot createRefinementSlot(QualifierHierarchy owner, QualifierLocation location, Slot refinedSlot) {
        Preconditions.checkNotNull(location);

        RefinementSlot slot = new AutoValue_RefinementSlot(slots.size(), owner, refinedSlot);
        slots.add(slot);
        slotLocator.addLocation(slot, location);
        return slot;
    }

    public SourceSlot createSourceSlot(QualifierHierarchy owner, @Nullable ConstantSlot defaultQualifier) {
        SourceSlot slot = new AutoValue_SourceSlot(slots.size(), owner, defaultQualifier);
        slots.add(slot);
        return slot;
    }

    public ViewpointAdaptationSlot createViewpointAdaptationSlot(QualifierHierarchy owner, QualifierLocation location, Slot receiverSlot, Slot declarationSlot) {
        Preconditions.checkNotNull(location);

        ViewpointAdaptationSlot slot = viewpointAdaptationSlots.computeIfAbsent(Pair.of(receiverSlot, declarationSlot),
                k -> new AutoValue_ViewpointAdaptationSlot(slots.size(), owner, receiverSlot, declarationSlot));

        if (slot.getId() == slots.size()) {
            slots.add(slot);
            slotLocator.addLocation(slot, location);
        }
        return slot;
    }

    public ProductSlot createProductSlot(Set<Slot> slots) {
        if (slots.isEmpty()) return EMPTY_PRODUCT_SLOT;

        Map<QualifierHierarchy, Slot> slotMap = new LinkedHashMap<>(slots.size());
        for (Slot slot : slots) {
            QualifierHierarchy hierarchy = slot.getOwner();
            Verify.verify(
                    !slotMap.containsKey(hierarchy),
                    "Found multiple slots in the same hierarchy %s in %s",
                    hierarchy.getClass().getSimpleName(),
                    slots
            );
            slotMap.put(hierarchy, slot);
        }
        return new AutoValue_ProductSlot(ImmutableMap.copyOf(slotMap));
    }

    public ProductSlot createProductSlot(Map<QualifierHierarchy, ? extends Slot> slots) {
        if (slots.isEmpty()) return EMPTY_PRODUCT_SLOT;

        for (Map.Entry<QualifierHierarchy, ? extends Slot> e : slots.entrySet()) {
            Verify.verify(e.getKey() == e.getValue().getOwner());
        }

        Set<Integer> ids = slots.values().stream().map(Slot::getId).collect(Collectors.toSet());
        return productSlotsCache.computeIfAbsent(ids, k -> new AutoValue_ProductSlot(ImmutableMap.copyOf(slots)));
    }
}
