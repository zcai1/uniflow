package org.cfginference.core.typesystem;

import com.google.common.collect.SetMultimap;
import org.cfginference.core.model.constraint.Constraint;
import org.cfginference.core.model.slot.ProductSlot;
import org.cfginference.core.model.type.QualifiedType;

public interface TypeHierarchy {

    SetMultimap<QualifierHierarchy, Constraint> getSubtypeConstraints(
            QualifiedType<ProductSlot> subType, QualifiedType<ProductSlot> superType);
}
