package org.cfginference.core.model.constraint;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.model.constraint.ArithmeticConstraint.ArithmeticOperation;
import org.cfginference.core.model.constraint.ComparisonConstraint.ComparisonOperation;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.reporting.CompilerMessage;
import org.cfginference.core.model.slot.ArithmeticSlot;
import org.cfginference.core.model.slot.ComparisonSlot;
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.MergeSlot;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.slot.SlotManager;
import org.cfginference.core.model.slot.VariableSlot;
import org.cfginference.core.model.slot.ViewpointAdaptationSlot;
import org.cfginference.core.typesystem.QualifierHierarchy;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ConstraintManager {

    private final SlotManager slotManager;

    private final SetMultimap<QualifierHierarchy, Constraint> effectiveConstraints;

    private final SetMultimap<Constraint, CompilerMessage> unsatMessages;

    private ConstraintManager(Context context) {
        this.slotManager = SlotManager.instance(context);
        this.effectiveConstraints = LinkedHashMultimap.create();
        this.unsatMessages = LinkedHashMultimap.create();

        context.put(ConstraintManager.class, this);
    }

    public static ConstraintManager instance(Context context) {
        ConstraintManager instance = context.get(ConstraintManager.class);
        if (instance == null) {
            instance = new ConstraintManager(context);
        }
        return instance;
    }

    private void addEffectiveConstraints(QualifierHierarchy owner,
                                         Set<Constraint> constraints,
                                         @Nullable CompilerMessage unsatMessage) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(constraints);

        for (Constraint c : constraints) {
            assert c != null;

            if (!(c instanceof AlwaysTrueConstraint)) {
                effectiveConstraints.put(owner, c);

                if (unsatMessage != null) {
                    unsatMessages.put(c, unsatMessage);
                }
            }
        }
    }

    private void addEffectiveConstraints(SetMultimap<QualifierHierarchy, Constraint> constraints,
                                         @Nullable CompilerMessage unsatMessage) {
        Objects.requireNonNull(constraints);

        for (QualifierHierarchy owner : constraints.keys()) {
            addEffectiveConstraints(owner, constraints.get(owner), unsatMessage);
        }
    }

    // Avoid using this method because no message will be provided if the constraint is unsat.
    public void addUnexplainedConstraint(QualifierHierarchy owner, Constraint constraint) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(constraint);

        addEffectiveConstraints(owner, Set.of(constraint), null);
    }

    public void addExplainedConstraints(QualifierHierarchy owner,
                                        Set<Constraint> constraints,
                                        CompilerMessage unsatMessage) {
        Objects.requireNonNull(unsatMessage);

        addEffectiveConstraints(owner, constraints, unsatMessage);
    }

    public void addExplainedConstraints(SetMultimap<QualifierHierarchy, Constraint> constraints,
                                        CompilerMessage unsatMessage) {
        Objects.requireNonNull(unsatMessage);

        addEffectiveConstraints(constraints, unsatMessage);
    }

    public SetMultimap<QualifierHierarchy, Constraint> getEffectiveConstraints() {
        return Multimaps.unmodifiableSetMultimap(effectiveConstraints);
    }

    public Constraint getForSubtype(Slot subtype, Slot superType) {
        Preconditions.checkArgument(subtype.getOwner() == superType.getOwner());

        QualifierHierarchy qualifierHierarchy = subtype.getOwner();
        Qualifier top = qualifierHierarchy.getTopQualifier();
        Qualifier bottom = qualifierHierarchy.getBottomQualifier();

        // Normalization cases:
        // C1 <: C2 => TRUE/FALSE depending on relationship
        // V1 <: TOP => TRUE
        // V1 <: BOTTOM => REPLACE_WITH_EQUALITY
        // BOTTOM <: V2 => TRUE
        // TOP <: V2 => REPLACE_WITH_EQUALITY
        // V == V => TRUE
        // V1 <: lub(V1, ...) => TRUE
        // V1 <: glb(V1, ...) => REPLACE_WITH_EQUALITY
        // lub(V2, ...) <: V2 => REPLACE_WITH_EQUALITY
        // glb(V2, ...) <: V2 => TRUE
        // otherwise => CREATE_REAL_SUBTYPE_CONSTRAINT

        if (subtype instanceof ConstantSlot constantSubtype && superType instanceof ConstantSlot constantSuperType) {
            return qualifierHierarchy.isSubtype(constantSubtype.getValue(), constantSuperType.getValue())
                    ? AlwaysTrueConstraint.instance()
                    : AlwaysFalseConstraint.instance();
        }

        if (subtype instanceof ConstantSlot constantSubtype) {
            if (top.equals(constantSubtype.getValue())) {
                return getForEquality(subtype, superType);
            }
            if (bottom.equals(constantSubtype.getValue())) {
                return AlwaysTrueConstraint.instance();
            }
        }

        if (superType instanceof ConstantSlot constantSuperType) {
            if (top.equals(constantSuperType.getValue())) {
                return AlwaysTrueConstraint.instance();
            }
            if (bottom.equals(constantSuperType.getValue())) {
                return getForEquality(subtype, superType);
            }
        }

        if (subtype.equals(superType)) {
            return AlwaysTrueConstraint.instance();
        }

        if (subtype instanceof MergeSlot mergeSubType) {
            if (slotManager.isMergedInto(superType, mergeSubType)) {
                if (mergeSubType.isLub()) {
                    return getForEquality(subtype, superType);
                } else {
                    return AlwaysTrueConstraint.instance();
                }
            }
        }

        if (superType instanceof MergeSlot mergeSuperType) {
            if (slotManager.isMergedInto(subtype, mergeSuperType)) {
                if (mergeSuperType.isLub()) {
                    return AlwaysTrueConstraint.instance();
                } else {
                    return getForEquality(subtype, superType);
                }
            }
        }

        return createSubtypeConstraint(subtype, superType);
    }

    public Constraint getForEquality(Slot first, Slot second) {
        Preconditions.checkArgument(first.getOwner() == second.getOwner());

        // Normalization cases:
        // C1 == C2 => TRUE/FALSE depending on annotation
        // V == V => TRUE
        // otherwise => CREATE_REAL_EQUALITY_CONSTRAINT

        if (first instanceof ConstantSlot firstConstant && second instanceof ConstantSlot secondConstant) {
            return firstConstant.getValue().equals(secondConstant.getValue())
                    ? AlwaysTrueConstraint.instance()
                    : AlwaysFalseConstraint.instance();
        }

        if (first.equals(second)) {
            return AlwaysTrueConstraint.instance();
        }

        return createEqualityConstraint(first, second);
    }

    public Constraint getForInequality(Slot first, Slot second) {
        Preconditions.checkArgument(first.getOwner() == second.getOwner());

        // Normalization cases:
        // C1 != C2 => TRUE/FALSE depending on annotation
        // V == V => FALSE
        // otherwise => CREATE_REAL_INEQUALITY_CONSTRAINT

        if (first instanceof ConstantSlot firstConstant && second instanceof ConstantSlot secondConstant) {
            return firstConstant.getValue().equals(secondConstant.getValue())
                    ? AlwaysFalseConstraint.instance()
                    : AlwaysTrueConstraint.instance();
        }

        if (first.equals(second)) {
            return AlwaysFalseConstraint.instance();
        }

        return createInequalityConstraint(first, second);
    }

    public Constraint getForComparable(Slot first, Slot second) {
        Preconditions.checkArgument(first.getOwner() == second.getOwner());

        QualifierHierarchy qualifierHierarchy = first.getOwner();

        // Normalization cases:
        // C1 <~> C2 => TRUE/FALSE depending on relationship
        // V <~> V => TRUE (every type is always comparable to itself)
        // otherwise => CREATE_REAL_COMPARABLE_CONSTRAINT

        if (first instanceof ConstantSlot firstConstant && second instanceof ConstantSlot secondConstant) {
            Qualifier c1 = firstConstant.getValue();
            Qualifier c2 = secondConstant.getValue();

            if (qualifierHierarchy.isSubtype(c1, c2) || qualifierHierarchy.isSubtype(c2, c1)) {
                return AlwaysTrueConstraint.instance();
            } else {
                return AlwaysFalseConstraint.instance();
            }
        }

        if (first.equals(second)) {
            return AlwaysTrueConstraint.instance();
        }

        return createComparableConstraint(first, second);
    }

    public Constraint getForComparison(ComparisonOperation op, Slot left, Slot right, ComparisonSlot result) {
        Preconditions.checkArgument(left.getOwner() == right.getOwner());
        Preconditions.checkArgument(left.getOwner() == result.getOwner());
        return createComparisonConstraint(op, left, right, result);
    }

    public Constraint getForViewpointAdaptation(ViewpointAdaptationSlot result) {
        return createViewpointAdaptationConstraint(result);
    }

    public Constraint getForPreference(VariableSlot variable, ConstantSlot goal, int weight) {
        Preconditions.checkArgument(variable.getOwner() == goal.getOwner());
        return createPreferenceConstraint(variable, goal, weight);
    }

    public Constraint getForExistential(Slot slot,
                                        List<Constraint> ifExistsConstraints,
                                        List<Constraint> ifNotExistsConstraints) {
        return createExistentialConstraint(slot, ifExistsConstraints, ifNotExistsConstraints);
    }

    public Set<Constraint> getForImplication(Collection<Constraint> assumptions, Constraint conclusion) {
        // Normalization cases:
        // 1) assumptions == empty ==> return conclusion
        // 2) conclusion == TRUE ==> return TRUE
        // 3) any assumption == FALSE ==> return TRUE
        // 4) refinedAssumptions == empty ==> return conclusion
        // 5) refinedAssumptions != empty && conclusion == FALSE ==> return
        // conjunction of refinedAssumptions
        // 6) refinedAssumptions != empty && conclusion != TRUE && conclusion !=
        // FALSE ==> CREATE_REAL_IMPLICATION_CONSTRAINT

        if (assumptions.isEmpty()) {
            return Set.of(conclusion);
        }

        if (conclusion instanceof AlwaysTrueConstraint) {
            return Set.of(AlwaysTrueConstraint.instance());
        }

        ImmutableSet.Builder<Constraint> refinedAssumptionsBuilder = new ImmutableSet.Builder<>();
        for (Constraint assumption : assumptions) {
            if (assumption instanceof AlwaysFalseConstraint) {
                return Set.of(AlwaysTrueConstraint.instance());
            }
            if (!(assumption instanceof AlwaysTrueConstraint)) {
                refinedAssumptionsBuilder.add(assumption);
            }
        }
        Set<Constraint> refinedAssumptions = refinedAssumptionsBuilder.build();

        if (refinedAssumptions.isEmpty()) {
            return Set.of(conclusion);
        }

        if (conclusion instanceof AlwaysFalseConstraint) {
            return refinedAssumptions;
        }

        return Set.of(createImplicationConstraint(refinedAssumptions, conclusion));
    }

    public Constraint getForArithmetic(ArithmeticOperation op, Slot left, Slot right, ArithmeticSlot result) {
        Preconditions.checkArgument(left.getOwner() == right.getOwner());
        return createArithmeticConstraint(op, left, right, result);
    }

    private static SubtypeConstraint createSubtypeConstraint(Slot subtype, Slot superType) {
        return new AutoValue_SubtypeConstraint(subtype, superType);
    }

    private static EqualityConstraint createEqualityConstraint(Slot first, Slot second) {
        return new AutoValue_EqualityConstraint(first, second);
    }

    private static InequalityConstraint createInequalityConstraint(Slot first, Slot second) {
        return new AutoValue_InequalityConstraint(first, second);
    }

    private static ComparableConstraint createComparableConstraint(Slot first, Slot second) {
        return new AutoValue_ComparableConstraint(first, second);
    }

    private static ComparisonConstraint createComparisonConstraint(ComparisonOperation op,
                                                                   Slot left,
                                                                   Slot right,
                                                                   ComparisonSlot result) {
        return new AutoValue_ComparisonConstraint(op, left, right, result);
    }

    private static ViewpointAdaptationConstraint createViewpointAdaptationConstraint(ViewpointAdaptationSlot result) {
        return new AutoValue_ViewpointAdaptationConstraint(result);
    }

    private static PreferenceConstraint createPreferenceConstraint(VariableSlot variable,
                                                                   ConstantSlot goal,
                                                                   int weight) {
        return new AutoValue_PreferenceConstraint(variable, goal, weight);
    }

    private static ExistentialConstraint createExistentialConstraint(Slot slot,
                                                                     Collection<Constraint> ifExistsConstraints,
                                                                     Collection<Constraint> ifNotExistsConstraints) {
        return new AutoValue_ExistentialConstraint(slot,
                ImmutableSet.copyOf(ifExistsConstraints),
                ImmutableSet.copyOf(ifNotExistsConstraints));
    }

    private static ImplicationConstraint createImplicationConstraint(Collection<Constraint> assumptions,
                                                                     Constraint conclusion) {
        return new AutoValue_ImplicationConstraint(ImmutableSet.copyOf(assumptions), conclusion);
    }

    private static ArithmeticConstraint createArithmeticConstraint(ArithmeticOperation op,
                                                                   Slot left,
                                                                   Slot right,
                                                                   ArithmeticSlot result) {
        return new AutoValue_ArithmeticConstraint(op, left, right, result);
    }
}
