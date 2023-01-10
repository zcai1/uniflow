package org.cfginference.core.flow;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.cfginference.util.AnnotationHelpers;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.UnderlyingAST;
import org.checkerframework.dataflow.cfg.builder.CFGTranslationPhaseOne;
import org.checkerframework.dataflow.cfg.builder.CFGTranslationPhaseThree;
import org.checkerframework.dataflow.cfg.builder.CFGTranslationPhaseTwo;
import org.checkerframework.dataflow.cfg.builder.PhaseOneResult;
import org.checkerframework.javacutil.AnnotationProvider;
import org.checkerframework.javacutil.BasicAnnotationProvider;
import org.checkerframework.javacutil.trees.TreeBuilder;

import javax.annotation.processing.ProcessingEnvironment;

public class CFGBuilder extends org.checkerframework.dataflow.cfg.builder.CFGBuilder {

    public static ControlFlowGraph build(
            TreePath bodyPath,
            UnderlyingAST underlyingAST,
            boolean assumeAssertionsEnabled,
            boolean assumeAssertionsDisabled,
            ProcessingEnvironment env,
            ArtificialTreeHandler artificialTreeHandler) {
        TreeBuilder builder = new TreeBuilder(env);
        TranslationPhaseOne phase1 = new TranslationPhaseOne(
                builder,
                AnnotationHelpers.DEFAULT_ANNO_PROVIDER,
                assumeAssertionsEnabled,
                assumeAssertionsDisabled,
                env,
                artificialTreeHandler
        );
        PhaseOneResult phase1result = phase1.process(bodyPath, underlyingAST);
        ControlFlowGraph phase2result = CFGTranslationPhaseTwo.process(phase1result);
        ControlFlowGraph phase3result = CFGTranslationPhaseThree.process(phase2result);
        return phase3result;
    }

    private static class TranslationPhaseOne extends CFGTranslationPhaseOne {

        private final ArtificialTreeHandler artificialTreeHandler;

        public TranslationPhaseOne(
                TreeBuilder treeBuilder,
                AnnotationProvider annotationProvider,
                boolean assumeAssertionsEnabled,
                boolean assumeAssertionsDisabled,
                ProcessingEnvironment env,
                ArtificialTreeHandler artificialTreeHandler) {
            super(treeBuilder, annotationProvider, assumeAssertionsEnabled, assumeAssertionsDisabled, env);
            this.artificialTreeHandler = artificialTreeHandler;
        }

        @Override
        public void handleArtificialTree(Tree tree) {
            artificialTreeHandler.handleArtificialTree(tree, getCurrentPath());
        }
    }
}
