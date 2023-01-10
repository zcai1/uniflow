package org.cfginference.core.manager;


import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.PluginOptions;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.UnderlyingAST;
import org.checkerframework.dataflow.cfg.builder.CFGBuilder;
import org.checkerframework.javacutil.TreePathUtil;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.LinkedHashMap;

public class InferenceCFGBuilder {

    public static LinkedHashMap<Tree, ControlFlowGraph> build(CompilationUnitTree cu, Context context) {
        CFGBuildVisitor visitor = new CFGBuildVisitor(context);
        visitor.scan(cu, null);
        return visitor.cfgs;
    }

    private static class CFGBuildVisitor extends TreePathScanner<Void, Void> {
        public final LinkedHashMap<Tree, ControlFlowGraph> cfgs = new LinkedHashMap<>();

        private ClassTree enclosingClass = null;

        private final PluginOptions options;

        private final ProcessingEnvironment env;

        public CFGBuildVisitor(Context context) {
            this.options = PluginOptions.instance(context);
            this.env = JavacProcessingEnvironment.instance(context);
        }

        @Override
        public Void visitClass(ClassTree tree, Void unused) {
            ClassTree prev = enclosingClass;
            enclosingClass = tree;
            try {
                super.visitClass(tree, unused);
            } finally {
                enclosingClass = prev;
            }
            return null;
        }

        @Override
        public Void visitMethod(MethodTree tree, Void unused) {
            super.visitMethod(tree, unused);

            TreePath path = getCurrentPath();
            assert path.getParentPath().getLeaf() == enclosingClass;

            if (tree.getBody() != null) {
                UnderlyingAST.CFGMethod method = new UnderlyingAST.CFGMethod(tree, enclosingClass);
                ControlFlowGraph cfg = CFGBuilder.build(
                        new TreePath(path, tree.getBody()),
                        method,
                        options.isAssertionEnabled(),
                        !options.isAssertionEnabled(),
                        env);
                cfgs.put(tree, cfg);
            }
            return null;
        }

        @Override
        public Void visitVariable(VariableTree tree, Void unused) {
            super.visitVariable(tree, unused);

            TreePath path = getCurrentPath();
            if (path.getParentPath().getLeaf() != enclosingClass || tree.getInitializer() == null) {
                return null;
            }

            UnderlyingAST.CFGStatement statement = new UnderlyingAST.CFGStatement(tree, enclosingClass);
            ControlFlowGraph cfg = CFGBuilder.build(
                    path,
                    statement,
                    options.isAssertionEnabled(),
                    !options.isAssertionEnabled(),
                    env);
            cfgs.put(tree, cfg);
            return null;
        }

        @Override
        public Void visitBlock(BlockTree tree, Void unused) {
            super.visitBlock(tree, unused);

            TreePath path = getCurrentPath();
            if (path.getParentPath().getLeaf() != enclosingClass) {
                return null;
            }

            UnderlyingAST.CFGStatement statement = new UnderlyingAST.CFGStatement(tree, enclosingClass);
            ControlFlowGraph cfg = CFGBuilder.build(
                    path,
                    statement,
                    options.isAssertionEnabled(),
                    !options.isAssertionEnabled(),
                    env);
            cfgs.put(tree, cfg);
            return null;
        }

        @Override
        public Void visitLambdaExpression(LambdaExpressionTree tree, Void unused) {
            super.visitLambdaExpression(tree, unused);

            TreePath path = getCurrentPath();
            MethodTree methodTree = TreePathUtil.enclosingMethod(path);
            UnderlyingAST.CFGLambda lambda = new UnderlyingAST.CFGLambda(tree, enclosingClass, methodTree);
            ControlFlowGraph cfg = CFGBuilder.build(
                    new TreePath(path, tree.getBody()),
                    lambda,
                    options.isAssertionEnabled(),
                    !options.isAssertionEnabled(),
                    env);
            cfgs.put(tree, cfg);
            return null;
        }
    }
}
