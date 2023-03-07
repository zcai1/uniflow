package org.uniflow.core.typesystem;

import com.sun.source.tree.Tree;
import org.uniflow.core.flow.FlowStore;
import org.uniflow.core.flow.FlowValue;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.cfg.block.SpecialBlock;
import org.checkerframework.dataflow.cfg.node.Node;

import javax.lang.model.element.Element;
import java.util.Map;

public interface TypeChecker {

    void checkDeclaration(Element element, @Nullable Tree tree);

    void checkNode(Node node, Map<Node, FlowValue> nodeValues);

    void checkSpecialBlock(SpecialBlock specialBlock, FlowStore store);
}
