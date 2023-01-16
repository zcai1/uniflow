package org.cfginference.core.solver.backend.encoder;

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
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.slot.VariableSlot;
import org.cfginference.core.solver.backend.encoder.binary.BinaryConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.binary.ComparableConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.binary.EqualityConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.binary.InequalityConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.binary.SubtypeConstraintEncoder;
import org.checkerframework.javacutil.BugInCF;

/**
 * A coordinator class that has the coordinating logic how each encoder encodes its supported
 * constraint.
 * <p>
 * Dispatching example: this class dispatches the encoding of {@link BinaryConstraint} to the
 * corresponding encodeXXX_YYY() method in {@link BinaryConstraintEncoder} depending on the
 * {@link SlotSlotCombo} of {@link BinaryConstraint} that the encoder encodes.
 * <p>
 * Redirecting example: this class simply redirects encoding of {@link PreferenceConstraint} to
 * {@link PreferenceConstraintEncoder#encode(PreferenceConstraint)} method, as this kind of
 * constraint doesn't need the {@code SlotSlotCombo} information to encode it.
 *
 * @see BinaryConstraintEncoder
 * @see ViewpointAdaptationConstraint
 * @see PreferenceConstraintEncoder
 * @see ExistentialConstraintEncoder
 */
public class ConstraintEncoderCoordinator {

    public static <ConstraintEncodingT> ConstraintEncodingT dispatch(
            SubtypeConstraint subtypeConstraint,
            SubtypeConstraintEncoder<ConstraintEncodingT> encoder) {
        return dispatchBinary(subtypeConstraint.getSubtype(), subtypeConstraint.getSupertype(), encoder);
    }

    public static <ConstraintEncodingT> ConstraintEncodingT dispatch(
            EqualityConstraint equalityConstraint,
            EqualityConstraintEncoder<ConstraintEncodingT> encoder) {
        return dispatchBinary(equalityConstraint.getFirst(), equalityConstraint.getSecond(), encoder);
    }

    public static <ConstraintEncodingT> ConstraintEncodingT dispatch(
            InequalityConstraint inequalityConstraint,
            InequalityConstraintEncoder<ConstraintEncodingT> encoder) {
        return dispatchBinary(inequalityConstraint.getFirst(), inequalityConstraint.getSecond(), encoder);
    }

    public static <ConstraintEncodingT> ConstraintEncodingT dispatch(
            ComparableConstraint comparableConstraint,
            ComparableConstraintEncoder<ConstraintEncodingT> encoder) {
        return dispatchBinary(comparableConstraint.getFirst(), comparableConstraint.getSecond(), encoder);
    }

    public static <ConstraintEncodingT> ConstraintEncodingT dispatch(
            ViewpointAdaptationConstraint constraint,
            ViewpointAdaptationConstraintEncoder<ConstraintEncodingT> encoder) {
        Slot target = constraint.getReceiverSlot();
        Slot declared = constraint.getDeclarationSlot();
        return switch (SlotSlotCombo.valueOf(target, declared)) {
            case VARIABLE_VARIABLE -> encoder.encodeVariable_Variable((VariableSlot) target,
                    (VariableSlot) declared,
                    constraint.getResult());
            case VARIABLE_CONSTANT -> encoder.encodeVariable_Constant((VariableSlot) target,
                    (ConstantSlot) declared,
                    constraint.getResult());
            case CONSTANT_VARIABLE -> encoder.encodeConstant_Variable((ConstantSlot) target,
                    (VariableSlot) declared,
                    constraint.getResult());
            case CONSTANT_CONSTANT -> encoder.encodeConstant_Constant((ConstantSlot) target,
                    (ConstantSlot) declared,
                    constraint.getResult());
            default -> throw new BugInCF("Unsupported SlotSlotCombo enum.");
        };
    }

    public static <ConstraintEncodingT> ConstraintEncodingT dispatch(
            ComparisonConstraint constraint,
            ComparisonConstraintEncoder<ConstraintEncodingT> encoder) {
        return switch (SlotSlotCombo.valueOf(constraint.getLeft(), constraint.getRight())) {
            case VARIABLE_VARIABLE -> encoder.encodeVariable_Variable(constraint.getOperation(),
                    (VariableSlot) constraint.getLeft(),
                    (VariableSlot) constraint.getRight(), constraint.getResult());
            case VARIABLE_CONSTANT -> encoder.encodeVariable_Constant(constraint.getOperation(),
                    (VariableSlot) constraint.getLeft(),
                    (ConstantSlot) constraint.getRight(), constraint.getResult());
            case CONSTANT_VARIABLE -> encoder.encodeConstant_Variable(constraint.getOperation(),
                    (ConstantSlot) constraint.getLeft(),
                    (VariableSlot) constraint.getRight(), constraint.getResult());
            case CONSTANT_CONSTANT -> encoder.encodeConstant_Constant(constraint.getOperation(),
                    (ConstantSlot) constraint.getLeft(),
                    (ConstantSlot) constraint.getRight(), constraint.getResult());
        };
    }

    public static <ConstraintEncodingT> ConstraintEncodingT dispatch(
            ArithmeticConstraint constraint,
            ArithmeticConstraintEncoder<ConstraintEncodingT> encoder) {
        Slot leftOperand = constraint.getLeft();
        Slot rightOperand = constraint.getRight();
        return switch (SlotSlotCombo.valueOf(leftOperand, rightOperand)) {
            case VARIABLE_VARIABLE -> encoder.encodeVariable_Variable(constraint.getOperation(),
                    (VariableSlot) leftOperand,
                    (VariableSlot) rightOperand, constraint.getResult());
            case VARIABLE_CONSTANT -> encoder.encodeVariable_Constant(constraint.getOperation(),
                    (VariableSlot) leftOperand,
                    (ConstantSlot) rightOperand, constraint.getResult());
            case CONSTANT_VARIABLE -> encoder.encodeConstant_Variable(constraint.getOperation(),
                    (ConstantSlot) leftOperand,
                    (VariableSlot) rightOperand, constraint.getResult());
            case CONSTANT_CONSTANT -> encoder.encodeConstant_Constant(constraint.getOperation(),
                    (ConstantSlot) leftOperand,
                    (ConstantSlot) rightOperand, constraint.getResult());
        };
    }

    public static <ConstraintEncodingT> ConstraintEncodingT redirect(
            PreferenceConstraint constraint,
            PreferenceConstraintEncoder<ConstraintEncodingT> encoder) {
        return encoder.encode(constraint);
    }

    public static <ConstraintEncodingT> ConstraintEncodingT redirect(
            ExistentialConstraint constraint,
            ExistentialConstraintEncoder<ConstraintEncodingT> encoder) {
        return encoder.encode(constraint);
    }

    public static <ConstraintEncodingT> ConstraintEncodingT redirect(
            ImplicationConstraint constraint,
            ImplicationConstraintEncoder<ConstraintEncodingT> encoder) {
        return encoder.encode(constraint);
    }

    private static <ConstraintEncodingT> ConstraintEncodingT dispatchBinary(
            Slot first, Slot second,
            BinaryConstraintEncoder<ConstraintEncodingT> encoder) {
        return switch (SlotSlotCombo.valueOf(first, second)) {
            case VARIABLE_VARIABLE -> encoder.encodeVariable_Variable((VariableSlot) first,
                    (VariableSlot) second);
            case VARIABLE_CONSTANT -> encoder.encodeVariable_Constant((VariableSlot) first,
                    (ConstantSlot) second);
            case CONSTANT_VARIABLE -> encoder.encodeConstant_Variable((ConstantSlot) first,
                    (VariableSlot) second);
            case CONSTANT_CONSTANT -> throw new BugInCF("Attempting to encode a constant-constant combination "
                    + "for a binary constraint. This should be normalized to "
                    + "either AlwaysTrueConstraint or AlwaysFalseConstraint.");
            default -> throw new BugInCF("Unsupported SlotSlotCombo enum.");
        };
    }
}
