package org.cfginference.core.typesystem;

import org.cfginference.core.model.slot.ProductSlot;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.type.QualifiedType;

public interface TypeHierarchy {
    // only allow for S = ProductSlot | Slot?
    <S extends Slot> QualifiedType<S> leastUpperBound(QualifiedType<S> type1, QualifiedType<S> type2);
}
