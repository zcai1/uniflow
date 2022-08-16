package org.cfginference.core.model.slot;

import org.cfginference.core.model.location.QualifierLocation;

public abstract class VariableSlot extends Slot {
    public abstract QualifierLocation getLocation();
}
