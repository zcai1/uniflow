package org.uniflow.core.typesystem;

import org.uniflow.core.flow.FlowStore;
import org.uniflow.core.flow.FlowValue;
import org.uniflow.core.model.slot.ProductSlot;
import org.uniflow.core.model.type.QualifiedType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.cfg.node.Node;

public interface NodeTypeResolver {

    @Nullable QualifiedType<ProductSlot> getType(Node node, TransferInput<FlowValue, FlowStore> input);

    QualifiedType<ProductSlot> getLhsNodeType(Node n);

    /**
     * Try to refine the {@code originalType} by using the {@code maybePreciseType} as a reference. Note that the
     * java type of {@code maybePreciseType} can be different from the java type of {@code originalType}.
     * Thus, the two types may have different structures.
     * <p>
     * For example, if there is an equal to expression `object == aString`, we may want to use the type
     * of RHS to refine the type of LHS.
     *
     * @param originalType the type to be refined
     * @param maybePreciseType the type that maybe provide precise information for refinement
     * @return the refined type
     */
    QualifiedType<ProductSlot> refineType(QualifiedType<ProductSlot> originalType,
                                          QualifiedType<ProductSlot> maybePreciseType);
}
