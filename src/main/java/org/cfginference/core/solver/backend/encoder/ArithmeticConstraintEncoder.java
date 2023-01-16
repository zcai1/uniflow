package org.cfginference.core.solver.backend.encoder;

import org.cfginference.core.model.constraint.ArithmeticConstraint.ArithmeticOperation;
import org.cfginference.core.model.slot.ArithmeticSlot;
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.VariableSlot;

/**
 * Interface that defines operations to encode a
 * {@link checkers.inference.model.ArithmeticConstraint}. It has four methods depending on the
 * {@link checkers.inference.solver.backend.encoder.SlotSlotCombo} of the {@code leftOperand} and
 * {@code rightOperand} slots.
 *
 * @see checkers.inference.model.ArithmeticConstraint
 */
public interface ArithmeticConstraintEncoder<ConstraintEncodingT> {
    ConstraintEncodingT encodeVariable_Variable(ArithmeticOperation operation,
                                                VariableSlot leftOperand,
                                                VariableSlot rightOperand,
                                                ArithmeticSlot result);

    ConstraintEncodingT encodeVariable_Constant(ArithmeticOperation operation,
                                                VariableSlot leftOperand,
                                                ConstantSlot rightOperand,
                                                ArithmeticSlot result);

    ConstraintEncodingT encodeConstant_Variable(ArithmeticOperation operation,
                                                ConstantSlot leftOperand,
                                                VariableSlot rightOperand,
                                                ArithmeticSlot result);

    ConstraintEncodingT encodeConstant_Constant(ArithmeticOperation operation,
                                                ConstantSlot leftOperand,
                                                ConstantSlot rightOperand,
                                                ArithmeticSlot result);
}
