package org.cfginference.core.model.util.serialization;

import org.cfginference.core.model.constraint.ArithmeticConstraint;
import org.cfginference.core.model.constraint.ComparableConstraint;
import org.cfginference.core.model.constraint.ComparisonConstraint;
import org.cfginference.core.model.constraint.EqualityConstraint;
import org.cfginference.core.model.constraint.ExistentialConstraint;
import org.cfginference.core.model.constraint.ImplicationConstraint;
import org.cfginference.core.model.constraint.InequalityConstraint;
import org.cfginference.core.model.constraint.PreferenceConstraint;
import org.cfginference.core.model.constraint.SubtypeConstraint;
import org.cfginference.core.model.constraint.ViewpointAdaptationConstraint;
import org.cfginference.core.model.slot.ArithmeticSlot;
import org.cfginference.core.model.slot.ComparisonSlot;
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.ExistentialSlot;
import org.cfginference.core.model.slot.MergeSlot;
import org.cfginference.core.model.slot.PolymorphicInstanceSlot;
import org.cfginference.core.model.slot.RefinementSlot;
import org.cfginference.core.model.slot.SourceSlot;
import org.cfginference.core.model.slot.ViewpointAdaptationSlot;

/**
 * Interface for serializing constraints and variables.
 * <p>
 * Serialization will occur for all variables and constraints either
 * before or instead of Constraint solving.
 * <p>
 * This allows us to avoid re-generating constraints for a piece of
 * source code every time we wish to solve (for instance when a new
 * solver is written or an existing one is modified).
 * <p>
 * Type parameters S and T are used to adapt the return type of the
 * XXXSlot visitor methods (S) and the XXXConstraint visitor methods
 * (T).
 * Implementing classes can use the same or different types for these
 * type parameters.
 */
public interface Serializer<S, T> {

    S serialize(SourceSlot slot);

    S serialize(ConstantSlot slot);

    S serialize(ExistentialSlot slot);

    S serialize(RefinementSlot slot);

    S serialize(ViewpointAdaptationSlot slot);

    S serialize(MergeSlot slot);

    S serialize(ComparisonSlot slot);

    S serialize(ArithmeticSlot slot);

    S serialize(PolymorphicInstanceSlot slot);

    T serialize(SubtypeConstraint constraint);

    T serialize(EqualityConstraint constraint);

    T serialize(ExistentialConstraint constraint);

    T serialize(InequalityConstraint constraint);

    T serialize(ComparableConstraint comparableConstraint);

    T serialize(ComparisonConstraint comparisonConstraint);

    T serialize(ViewpointAdaptationConstraint combineConstraint);

    T serialize(PreferenceConstraint preferenceConstraint);

    T serialize(ImplicationConstraint implicationConstraint);

    T serialize(ArithmeticConstraint arithmeticConstraint);
}
