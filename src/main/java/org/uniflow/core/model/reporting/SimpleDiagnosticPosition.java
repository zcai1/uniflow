package org.uniflow.core.model.reporting;

import com.google.auto.value.AutoValue;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.JCDiagnostic;

@AutoValue
public abstract class SimpleDiagnosticPosition implements JCDiagnostic.DiagnosticPosition {

    public abstract int getEndPosition();

    // Don't save tree to prevent memory leak.
    @Override
    public JCTree getTree() {
        return null;
    }

    @Override
    public int getEndPosition(EndPosTable endPosTable) {
        return getEndPosition();
    }

    public static SimpleDiagnosticPosition create(CompilationUnitTree root, Tree tree) {
        JCTree jcTree = (JCTree) tree;
        return new AutoValue_SimpleDiagnosticPosition(jcTree.getStartPosition(),
                jcTree.getPreferredPosition(),
                jcTree.getEndPosition(((JCTree.JCCompilationUnit) root).endPositions));
    }
}
