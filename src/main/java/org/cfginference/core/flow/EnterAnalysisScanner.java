package org.cfginference.core.flow;

import com.google.common.base.Verify;
import com.google.common.collect.Comparators;
import com.google.common.collect.Iterables;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.PluginOptions;
import org.cfginference.core.event.Event;
import org.cfginference.core.event.EventListener;
import org.cfginference.core.event.EventManager;
import org.cfginference.core.model.error.PluginError;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.AnalysisResult;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.UnderlyingAST;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TreePathUtil;
import org.checkerframework.javacutil.TreeUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.VariableElement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

// TODO: Provide field initial values (e.g., use RHS type for static immutable fields, for initialization checks)
// TODO: pre and post conditions
public final class EnterAnalysisScanner extends TreePathScanner<Void, Void> implements EventListener {

    private static final Comparator<Tree> initializationOrder = Comparator.comparing(InitializationOrder::from);

    private final PluginOptions options;

    private final ProcessingEnvironment env;

    private final FlowContext flowContext;

    private final FlowAnalysis flowAnalysis;

    private final FlowTransfer flowTransfer;

    private final Map<Tree, @Nullable FlowStore> capturedStores;

    private final Deque<ClassTree> enclosingClasses;

    private final Deque<FlowStore> initializationStores;

    private final Deque<FlowStore> initializationStaticStores;

    public static EnterAnalysisScanner instance(Context context) {
        EnterAnalysisScanner instance = context.get(EnterAnalysisScanner.class);
        if (instance == null) {
            instance = new EnterAnalysisScanner(context);
        }
        return instance;
    }

    private EnterAnalysisScanner(Context context) {
        this.options = PluginOptions.instance(context);
        this.env = JavacProcessingEnvironment.instance(context);
        this.flowContext = FlowContext.instance(context);
        this.flowAnalysis = FlowAnalysis.instance(context);
        this.flowTransfer = FlowTransfer.instance(context);
        this.capturedStores = new IdentityHashMap<>();
        this.enclosingClasses = new ArrayDeque<>();
        this.initializationStores = new ArrayDeque<>();
        this.initializationStaticStores = new ArrayDeque<>();

        EventManager eventManager = EventManager.instance(context);
        eventManager.register(this);

        context.put(EnterAnalysisScanner.class, this);
    }

    @Override
    public void finished(Event e) {
        if (e instanceof Event.NewAnalysisTask) {
            capturedStores.clear();
            Verify.verify(enclosingClasses.isEmpty());
            Verify.verify(initializationStores.isEmpty());
            Verify.verify(initializationStaticStores.isEmpty());
        }
    }

    @Override
    public Void visitClass(ClassTree tree, Void unused) {
        if (tree.getKind() == Tree.Kind.ANNOTATION_TYPE) {
            return null;
        }

        if (TreeUtils.elementFromDeclaration(tree).getNestingKind() == NestingKind.MEMBER) {
            // Use the enclosing class's captured store for static inner classes
            capturedStores.put(tree, capturedStores.get(enclosingClasses.peek()));
        }
        initializationStores.push(capturedStores.get(tree));
        initializationStaticStores.push(capturedStores.get(tree));
        enclosingClasses.push(tree);

        try {
            List<? extends Tree> members = tree.getMembers();
            if (!Comparators.isInOrder(members, initializationOrder)) {
                members = new ArrayList<>(tree.getMembers());
                members.sort(initializationOrder);
            }
            return scan(members, null);
        } finally {
            initializationStores.pop();
            initializationStaticStores.pop();
            enclosingClasses.pop();
        }
    }

    @Override
    public Void visitMethod(MethodTree tree, Void unused) {
        TreePath path = getCurrentPath();
        ClassTree enclosingClass = enclosingClasses.peek();
        assert path.getParentPath().getLeaf() == enclosingClass;

        if (tree.getBody() == null) {
            // TODO: probably still need to perform inheritance check
            return null;
        }

        UnderlyingAST.CFGMethod method = new UnderlyingAST.CFGMethod(tree, enclosingClass);
        ControlFlowGraph cfg = CFGBuilder.build(
                new TreePath(path, tree.getBody()),
                method,
                options.isAssertionEnabled(),
                !options.isAssertionEnabled(),
                env,
                flowContext);

        process(tree, cfg);
        return super.visitMethod(tree, unused);
    }

    @Override
    public Void visitVariable(VariableTree tree, Void unused) {
        VariableElement element = TreeUtils.elementFromDeclaration(tree);
        assert element != null;

        if (tree.getInitializer() == null
                || (element.getKind() != ElementKind.FIELD && element.getKind() != ElementKind.ENUM_CONSTANT)) {
            return null;
        }

        ClassTree enclosingClass = enclosingClasses.peek();
        UnderlyingAST.CFGStatement statement = new UnderlyingAST.CFGStatement(tree, enclosingClass);
        ControlFlowGraph cfg = CFGBuilder.build(
                getCurrentPath(),
                statement,
                options.isAssertionEnabled(),
                !options.isAssertionEnabled(),
                env,
                flowContext);

        process(tree, cfg);
        return super.visitVariable(tree, unused);
    }

    @Override
    public Void visitBlock(BlockTree tree, Void unused) {
        TreePath path = getCurrentPath();
        ClassTree enclosingClass = enclosingClasses.peek();
        if (path.getParentPath().getLeaf() != enclosingClass) {
            return null;
        }

        UnderlyingAST.CFGStatement statement = new UnderlyingAST.CFGStatement(tree, enclosingClass);
        ControlFlowGraph cfg = CFGBuilder.build(
                path,
                statement,
                options.isAssertionEnabled(),
                !options.isAssertionEnabled(),
                env,
                flowContext);

        process(tree, cfg);
        return super.visitBlock(tree, unused);
    }

    @Override
    public Void visitLambdaExpression(LambdaExpressionTree tree, Void unused) {
        TreePath path = getCurrentPath();
        MethodTree methodTree = TreePathUtil.enclosingMethod(path);
        ClassTree enclosingClass = enclosingClasses.peek();
        UnderlyingAST.CFGLambda lambda = new UnderlyingAST.CFGLambda(tree, enclosingClass, methodTree);
        ControlFlowGraph cfg = CFGBuilder.build(
                new TreePath(path, tree.getBody()),
                lambda,
                options.isAssertionEnabled(),
                !options.isAssertionEnabled(),
                env,
                flowContext);

        process(tree, cfg);
        return super.visitLambdaExpression(tree, unused);
    }

    private void process(Tree tree, ControlFlowGraph cfg) {
        resolveTypes(tree, cfg);
        // TODO: type checks, inheritance check, source slot locating (?), visualization
    }

    private void resolveTypes(Tree tree, ControlFlowGraph cfg) {
        Deque<FlowStore> useInitializationStore = null;
        boolean updateInitializationStore = false;

        switch (InitializationOrder.from(tree)) {
            case STATIC_LEVEL -> {
                useInitializationStore = initializationStaticStores;
                updateInitializationStore = true;
            }
            case INSTANCE_LEVEL -> {
                useInitializationStore = initializationStores;
                updateInitializationStore = true;
            }
            case CONSTRUCTOR -> {
                useInitializationStore = initializationStores;
            }
            default -> Verify.verify(tree.getKind() == Tree.Kind.LAMBDA_EXPRESSION,
                    "Unexpected tree to analyze: %s",
                    tree.getKind());
        }

        FlowStore fixedInitStore;
        if (useInitializationStore != null) {
            fixedInitStore = useInitializationStore.peek();
        } else {
            fixedInitStore = capturedStores.get(tree);
            if (fixedInitStore == null) {
                fixedInitStore = capturedStores.get(enclosingClasses.peek());
            }
        }

        flowTransfer.setFixedInitialStore(fixedInitStore);
        flowAnalysis.performAnalysis(cfg);
        flowContext.combineResult(flowAnalysis.getResult());

        Iterable<Tree> innerDecls = Iterables.concat(cfg.getDeclaredClasses(), cfg.getDeclaredLambdas());
        for (Tree decl : innerDecls) {
            capturedStores.put(decl, flowAnalysis.getStoreBefore(decl));
        }

        if (updateInitializationStore) {
            assert useInitializationStore != null;
            useInitializationStore.pop();
            useInitializationStore.push(flowAnalysis.getRegularExitStore());
        }
    }
}
