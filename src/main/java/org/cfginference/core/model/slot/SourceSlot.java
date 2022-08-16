package org.cfginference.core.model.slot;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.location.QualifierLocation;
import org.cfginference.core.model.qualifier.Qualifier;
import org.checkerframework.checker.nullness.qual.Nullable;

@AutoValue
public abstract class SourceSlot extends VariableSlot {
    @Override
    public abstract boolean isInsertable();
    @Nullable
    public abstract Qualifier getDefaultQualifier();

    public static SourceSlot create(int id, QualifierLocation location, boolean insertable) {
        return new AutoValue_SourceSlot(id, location, insertable, null);
    }

    public static SourceSlot create(int id,
                                    QualifierLocation location,
                                    boolean insertable,
                                    @Nullable Qualifier defaultQualifier) {
        return new AutoValue_SourceSlot(id, location, insertable, defaultQualifier);
    }
}
