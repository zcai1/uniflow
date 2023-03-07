package org.uniflow.core.typesystem;

import com.google.common.collect.SetMultimap;
import org.uniflow.core.model.constraint.Constraint;
import org.uniflow.core.model.slot.ProductSlot;
import org.uniflow.core.model.type.QualifiedType;

public interface TypeHierarchy {

    SetMultimap<QualifierHierarchy, Constraint> getSubtypeConstraints(
            QualifiedType<ProductSlot> subType, QualifiedType<ProductSlot> superType);
}
