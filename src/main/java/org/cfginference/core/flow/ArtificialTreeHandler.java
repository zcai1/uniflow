package org.cfginference.core.flow;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;

public interface ArtificialTreeHandler {
    void handleArtificialTree(Tree artificialTree, TreePath currentPath);
}
