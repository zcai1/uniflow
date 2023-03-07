package org.uniflow.core.flow;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
interface ArtificialTreeHandler {
    void handleArtificialTree(Tree artificialTree, TreePath currentPath);
}
