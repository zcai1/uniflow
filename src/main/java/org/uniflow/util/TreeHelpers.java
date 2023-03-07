package org.uniflow.util;

import com.google.common.base.Preconditions;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;

public class TreeHelpers {
    public static boolean isArrayLiteral(NewArrayTree newArrayTree) {
        return newArrayTree.getType() == null;
    }

    public static Element findEnclosingElementForLambda(TreePath path) {
        Preconditions.checkArgument(path.getLeaf().getKind() == Tree.Kind.LAMBDA_EXPRESSION);

        ClassTree enclosingClass = null;
        TreePath currentPath = path;
        while (currentPath != null) {
            Tree currentLeaf = currentPath.getLeaf();

            if (TreeUtils.isClassTree(currentLeaf)) {
                // find innermost class
                enclosingClass = (ClassTree) currentLeaf;
                break;
            } else if (currentLeaf.getKind() == Tree.Kind.METHOD) {
                // find innermost method
                MethodTree enclosingMethod = (MethodTree) currentLeaf;
                return TreeUtils.elementFromDeclaration(enclosingMethod);
            }

            currentPath = currentPath.getParentPath();
        }

        // lambda isn't declared in a method, now try to find enclosing static or instance initializer
        assert enclosingClass != null;
        currentPath = path;
        while (currentPath.getLeaf() != enclosingClass) {
            Tree currentLeaf = currentPath.getLeaf();

            // If there's an enclosing block, we try to find a variable declaration whose element is enclosed
            // by the initializer's element.
            if (currentLeaf.getKind() == Tree.Kind.BLOCK) {
                BlockTree enclosingBlock = (BlockTree) currentLeaf;
                for (StatementTree statement : enclosingBlock.getStatements()) {
                    if (statement.getKind() == Tree.Kind.VARIABLE) {
                        VariableElement varElement = TreeUtils.elementFromDeclaration((VariableTree) statement);
                        Element varEnclosingElement = varElement.getEnclosingElement();
                        if (varEnclosingElement.getKind() == ElementKind.STATIC_INIT
                                || varEnclosingElement.getKind() == ElementKind.INSTANCE_INIT) {
                            return varEnclosingElement;
                        }
                    }
                }
            }

            currentPath = currentPath.getParentPath();
        }

        // couldn't find an enclosing executable element, use class element as default
        return TreeUtils.elementFromDeclaration(enclosingClass);
    }
}
