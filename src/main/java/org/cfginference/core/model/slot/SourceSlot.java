package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.location.QualifierLocation;
import org.cfginference.core.model.qualifier.Qualifier;
import org.checkerframework.checker.nullness.qual.Nullable;

@AutoValue
public abstract class SourceSlot extends VariableSlot {
    @Nullable
    public abstract Qualifier getDefaultQualifier();
}
