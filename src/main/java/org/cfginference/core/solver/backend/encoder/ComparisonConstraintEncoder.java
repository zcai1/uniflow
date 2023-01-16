package org.cfginference.core.solver.backend.encoder;

import org.cfginference.core.model.constraint.ComparisonConstraint;
import org.cfginference.core.model.constraint.ComparisonConstraint.ComparisonOperation;
import org.cfginference.core.model.slot.ComparisonSlot;
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.VariableSlot;

/**
 * A marker interface that all constraint encoders that support encoding {@link checkers.inference.model.ComparisonConstraint}
 * should implement. Otherwise, the encoder will be considered not supporting encoding comparable
 * constraint and rejected by the {@link AbstractConstraintEncoderFactory#createComparisonConstraintEncoder()}
 *
 * @see checkers.inference.model.ComparisonConstraint
 * @see AbstractConstraintEncoderFactory#createComparisonConstraintEncoder()
 */
public interface ComparisonConstraintEncoder<ConstraintEncodingT> {
    ConstraintEncodingT encodeVariable_Variable(ComparisonOperation operation,
                                                VariableSlot left,
                                                VariableSlot right,
                                                ComparisonSlot result);

    ConstraintEncodingT encodeVariable_Constant(ComparisonOperation operation,
                                                VariableSlot left,
                                                ConstantSlot right,
                                                ComparisonSlot result);

    ConstraintEncodingT encodeConstant_Variable(ComparisonOperation operation,
                                                ConstantSlot left,
                                                VariableSlot right,
                                                ComparisonSlot result);

    ConstraintEncodingT encodeConstant_Constant(ComparisonOperation operation,
                                                ConstantSlot left,
                                                ConstantSlot right,
                                                ComparisonSlot result);
}
