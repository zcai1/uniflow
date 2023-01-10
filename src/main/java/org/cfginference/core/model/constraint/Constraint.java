package org.cfginference.core.model.constraint;

import com.google.common.collect.ImmutableList;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.util.serialization.Serializer;
import org.cfginference.core.typesystem.QualifierHierarchy;

public abstract class Constraint {

    public abstract ImmutableList<Slot> getSlots();

    // /**
    //  * The qualifier hierarchy that owns this constraint.
    //  * NOTE: This doesn't mean all associated slots are owned by the same qualifier hierarchy.
    //  *  For example, @Uninitialized => @Nullable could be a constraint owned by nullness hierarchy.
    //  *
    //  * @return The qualifier hierarchy that owns this constraint.
    //  */
    // public abstract QualifierHierarchy getOwner();

    public abstract <S, T> T serialize(Serializer<S, T> serializer);
}
