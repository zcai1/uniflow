package org.cfginference.core.model.slot;

import org.cfginference.core.model.location.QualifierLocation;

/**
 * ArithmeticVariableSlot represent the result of an arithmetic operation between two other
 * {@link Slot}s. Note that this slot is serialized identically to a {@link Slot}.
 */
public class ArithmeticSlot extends Slot {

    /**
     * Constructor
     * @param location location of the slot
     */
    public ArithmeticSlot(QualifierLocation location) {
        super(location);
    }

    /**
     * ArithmeticVariables should never be re-inserted into the source code.
     *
     * @return false
     */
    @Override
    public boolean isInsertable() {
        return false;
    }

    @Override
    public Kind getKind() {
        return Kind.ARITHMETIC;
    }
}
