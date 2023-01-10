package org.cfginference.util;

import com.sun.source.tree.Tree;
import org.cfginference.core.flow.FlowContext;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.cfg.node.Node;

public class NodeUtils {

    private NodeUtils() {}

    public static @Nullable Tree getRealSourceTree(FlowContext flowContext, @Nullable Node node) {
        if (node == null) {
            return null;
        }

        Tree sourceTree = node.getTree();
        if (sourceTree == null) {
            return null;
        }

        if (flowContext.isArtificialTree(sourceTree)) {
            return flowContext.getPathToArtificialTreeParent(sourceTree).getLeaf();
        }
        return sourceTree;
    }
}
