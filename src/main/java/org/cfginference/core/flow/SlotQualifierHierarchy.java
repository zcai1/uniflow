package org.cfginference.core.flow;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.model.constraint.AlwaysTrueConstraint;
import org.cfginference.core.model.constraint.Constraint;
import org.cfginference.core.model.constraint.ConstraintManager;
import org.cfginference.core.model.location.LocationManager;
import org.cfginference.core.model.location.NodeLocation;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.MergeSlot;
import org.cfginference.core.model.slot.ProductSlot;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.slot.SlotManager;
import org.cfginference.core.typesystem.QualifierHierarchy;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public final class SlotQualifierHierarchy {

    private final ConstraintManager constraintManager;

    private final SlotManager slotManager;

    private final LocationManager locationManager;

    private final FlowContext flowContext;

    private SlotQualifierHierarchy(Context context) {
        constraintManager = ConstraintManager.instance(context);
        slotManager = SlotManager.instance(context);
        locationManager = LocationManager.instance(context);
        flowContext = FlowContext.instance(context);

        context.put(SlotQualifierHierarchy.class, this);
    }

    public static SlotQualifierHierarchy instance(Context context) {
        SlotQualifierHierarchy instance = context.get(SlotQualifierHierarchy.class);
        if (instance == null) {
            instance = new SlotQualifierHierarchy(context);
        }
        return instance;
    }

    public Constraint getSubtypeConstraint(Slot subSlot, Slot superSlot) {
        return constraintManager.getForSubtype(subSlot, superSlot);
    }

    public SetMultimap<QualifierHierarchy, Constraint> getSubtypeConstraints(ProductSlot subProduct,
                                                                             ProductSlot superProduct,
                                                                             Set<QualifierHierarchy> forHierarchies) {
        SetMultimap<QualifierHierarchy, Constraint> result = LinkedHashMultimap.create();
        for (QualifierHierarchy hierarchy : forHierarchies) {
            Slot subSlot = Objects.requireNonNull(subProduct.getSlotByHierarchy(hierarchy));
            Slot superSlot = Objects.requireNonNull(superProduct.getSlotByHierarchy(hierarchy));
            result.put(hierarchy, getSubtypeConstraint(subSlot, superSlot));
        }
        return Multimaps.unmodifiableSetMultimap(result);
    }

    public Constraint getSameTypeConstraint(Slot slot1, Slot slot2) {
        return constraintManager.getForEquality(slot1, slot2);
    }

    public SetMultimap<QualifierHierarchy, Constraint> getEqualityConstraints(ProductSlot product1,
                                                                              ProductSlot product2,
                                                                              Set<QualifierHierarchy> forHierarchies) {
        SetMultimap<QualifierHierarchy, Constraint> result = LinkedHashMultimap.create();
        for (QualifierHierarchy hierarchy : forHierarchies) {
            Slot subSlot = Objects.requireNonNull(product1.getSlotByHierarchy(hierarchy));
            Slot superSlot = Objects.requireNonNull(product2.getSlotByHierarchy(hierarchy));
            result.put(hierarchy, getSameTypeConstraint(subSlot, superSlot));
        }
        return Multimaps.unmodifiableSetMultimap(result);
    }

    public Slot leastUpperBound(Slot slot1, Slot slot2) {
        Preconditions.checkArgument(slot1.getOwner() == slot2.getOwner());
        QualifierHierarchy qualifierHierarchy = slot1.getOwner();

        // case 1: both are constants => lub(c1, c2)
        if (slot1 instanceof ConstantSlot constant1 && slot2 instanceof ConstantSlot constant2) {
            Qualifier lub = qualifierHierarchy.leastUpperBound(constant1.getValue(), constant2.getValue());
            return slotManager.createConstantSlot(qualifierHierarchy, lub);
        }

        // case 2: slot1 <: slot2 => slot2
        if (getSubtypeConstraint(slot1, slot2) instanceof AlwaysTrueConstraint) {
            return slot2;
        }

        // case 3: slot2 <: slot1 => slot1
        if (getSubtypeConstraint(slot2, slot1) instanceof AlwaysTrueConstraint) {
            return slot1;
        }

        // case 4: create a merge slot
        NodeLocation nodeLocation = locationManager.getNodeLocation();
        MergeSlot mergeSlot = slotManager.createMergeSlot(qualifierHierarchy, nodeLocation, slot1, slot2, true);
        // create constraints
        Constraint mergeIsSuperOfSlot1 = getSubtypeConstraint(slot1, mergeSlot);
        Constraint mergeIsSuperOfSlot2 = getSubtypeConstraint(slot2, mergeSlot);
        constraintManager.addUnexplainedConstraint(qualifierHierarchy, mergeIsSuperOfSlot1);
        constraintManager.addUnexplainedConstraint(qualifierHierarchy, mergeIsSuperOfSlot2);
        return mergeSlot;
    }

    public Slot leastUpperBound(Collection<? extends Slot> slots) {
        Preconditions.checkArgument(!slots.isEmpty());

        Iterator<? extends Slot> iter = slots.iterator();
        Slot result = iter.next();

        while (iter.hasNext()) {
            result = leastUpperBound(result, iter.next());
        }
        return result;
    }

    public Slot greatestLowerBound(Slot slot1, Slot slot2) {
        Preconditions.checkArgument(slot1.getOwner() == slot2.getOwner());
        QualifierHierarchy qualifierHierarchy = slot1.getOwner();

        // case 1: both are constants => lub(c1, c2)
        if (slot1 instanceof ConstantSlot constant1 && slot2 instanceof ConstantSlot constant2) {
            Qualifier glb = qualifierHierarchy.greatestLowerBound(constant1.getValue(), constant2.getValue());
            return slotManager.createConstantSlot(qualifierHierarchy, glb);
        }

        // case 2: slot1 <: slot2 => slot1
        if (getSubtypeConstraint(slot1, slot2) instanceof AlwaysTrueConstraint) {
            return slot1;
        }

        // case 3: slot2 <: slot1 => slot2
        if (getSubtypeConstraint(slot2, slot1) instanceof AlwaysTrueConstraint) {
            return slot2;
        }

        // case 4: create a merge slot
        NodeLocation nodeLocation = locationManager.getNodeLocation();
        MergeSlot mergeSlot = slotManager.createMergeSlot(qualifierHierarchy, nodeLocation, slot1, slot2, false);
        Constraint mergeIsSubOfSlot1 = getSubtypeConstraint(mergeSlot, slot1);
        Constraint mergeIsSubOfSlot2 = getSubtypeConstraint(mergeSlot, slot2);
        constraintManager.addUnexplainedConstraint(qualifierHierarchy, mergeIsSubOfSlot1);
        constraintManager.addUnexplainedConstraint(qualifierHierarchy, mergeIsSubOfSlot2);
        return mergeSlot;
    }

    public Slot greatestLowerBound(Collection<? extends Slot> slots) {
        Preconditions.checkArgument(!slots.isEmpty());

        Iterator<? extends Slot> iter = slots.iterator();
        Slot result = iter.next();

        while (iter.hasNext()) {
            result = greatestLowerBound(result, iter.next());
        }
        return result;
    }
}
