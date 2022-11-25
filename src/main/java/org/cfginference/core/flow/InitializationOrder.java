package org.cfginference.core.flow;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.element.VariableElement;

public enum InitializationOrder {
    STATIC_LEVEL, // static variables and static initializers
    INSTANCE_LEVEL, // instance variables and instance initializers
    CONSTRUCTOR, // constructors
    OTHER; // others

    public static InitializationOrder from(Tree tree) {
        switch (tree.getKind()) {
            case VARIABLE -> {
                VariableTree variableTree = (VariableTree) tree;
                VariableElement element = TreeUtils.elementFromDeclaration(variableTree);
                if (ElementUtils.isStatic(element)) {
                    return STATIC_LEVEL;
                }
                return INSTANCE_LEVEL;
            }
            case BLOCK -> {
                BlockTree blockTree = (BlockTree) tree;
                if (blockTree.isStatic()) {
                    return STATIC_LEVEL;
                }
                return INSTANCE_LEVEL;
            }
            case METHOD -> {
                MethodTree methodTree = (MethodTree) tree;
                if (TreeUtils.isConstructor(methodTree)) {
                    return CONSTRUCTOR;
                }
                return OTHER;
            }
            default -> {
                return OTHER;
            }
        }
    }
}
