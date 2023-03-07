package org.uniflow.core.model.util.serialization;

import org.uniflow.core.model.constraint.ArithmeticConstraint;
import org.uniflow.core.model.constraint.ComparableConstraint;
import org.uniflow.core.model.constraint.ComparisonConstraint;
import org.uniflow.core.model.constraint.EqualityConstraint;
import org.uniflow.core.model.constraint.ExistentialConstraint;
import org.uniflow.core.model.constraint.ImplicationConstraint;
import org.uniflow.core.model.constraint.InequalityConstraint;
import org.uniflow.core.model.constraint.PreferenceConstraint;
import org.uniflow.core.model.constraint.SubtypeConstraint;
import org.uniflow.core.model.constraint.ViewpointAdaptationConstraint;
import org.uniflow.core.model.slot.ArithmeticSlot;
import org.uniflow.core.model.slot.ComparisonSlot;
import org.uniflow.core.model.slot.ConstantSlot;
import org.uniflow.core.model.slot.ExistentialSlot;
import org.uniflow.core.model.slot.MergeSlot;
import org.uniflow.core.model.slot.PolymorphicInstanceSlot;
import org.uniflow.core.model.slot.RefinementSlot;
import org.uniflow.core.model.slot.SourceSlot;
import org.uniflow.core.model.slot.ViewpointAdaptationSlot;

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

    T serialize(ComparableConstraint constraint);

    T serialize(ComparisonConstraint constraint);

    T serialize(ViewpointAdaptationConstraint constraint);

    T serialize(PreferenceConstraint constraint);

    T serialize(ImplicationConstraint constraint);

    T serialize(ArithmeticConstraint constraint);
}
