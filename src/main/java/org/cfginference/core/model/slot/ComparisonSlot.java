package org.cfginference.core.model.slot;

import org.cfginference.core.model.location.QualifierLocation;

/**
 * ComparisonVariableSlot represent the left side refinement of an comparison operation between two other
 * {@link Slot}s.
 * e.g., for a comparison constraint c := x &lt y, the comparison variable slot c is the refined value of
 * x where x < y is always when x = c.
 */
public class ComparisonSlot extends Slot {

    public ComparisonSlot(QualifierLocation location) {
        super(location);
    }

    @Override
    public boolean isInsertable() {
        return false;
    }

    @Override
    public Kind getKind() {
        return Kind.COMPARISON;
    }
}
