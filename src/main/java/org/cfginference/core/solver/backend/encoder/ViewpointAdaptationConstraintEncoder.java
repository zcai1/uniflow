package org.cfginference.core.solver.backend.encoder;

import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.VariableSlot;
import org.cfginference.core.model.slot.ViewpointAdaptationSlot;

/**
 * Interface that defines operations to encode a {@link checkers.inference.model.CombineConstraint}. It has four methods
 * depending on the {@link checkers.inference.solver.backend.encoder.SlotSlotCombo} of {@code target} and {@code
 * declared} slots.
 *
 * <p>
 * {@code result} is always {@link checkers.inference.model.CombVariableSlot}, which is essentially {@link Slot},
 * whose {@link Slot#id} is the only interesting knowledge in encoding phase. Therefore there don't exist
 * methods in which {@code result} is {@link ConstantSlot}.
 *
 * @see checkers.inference.model.CombineConstraint
 * @see checkers.inference.solver.backend.encoder.SlotSlotCombo
 */
public interface ViewpointAdaptationConstraintEncoder<ConstraintEncodingT> {

    ConstraintEncodingT encodeVariable_Variable(VariableSlot target,
                                                VariableSlot declared,
                                                ViewpointAdaptationSlot result);

    ConstraintEncodingT encodeVariable_Constant(VariableSlot target,
                                                ConstantSlot declared,
                                                ViewpointAdaptationSlot result);

    ConstraintEncodingT encodeConstant_Variable(ConstantSlot target,
                                                VariableSlot declared,
                                                ViewpointAdaptationSlot result);

    ConstraintEncodingT encodeConstant_Constant(ConstantSlot target,
                                                ConstantSlot declared,
                                                ViewpointAdaptationSlot result);
}
