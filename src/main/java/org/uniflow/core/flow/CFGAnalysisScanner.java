package org.uniflow.core.flow;

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
import org.uniflow.core.PluginOptions;
import org.uniflow.core.TypeSystems;
import org.uniflow.core.event.Event;
import org.uniflow.core.event.EventListener;
import org.uniflow.core.event.EventManager;
import org.uniflow.core.typesystem.TypeSystem;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.UnderlyingAST;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.block.SpecialBlock;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.visualize.CFGVisualizer;
import org.checkerframework.dataflow.cfg.visualize.DOTCFGVisualizer;
import org.checkerframework.javacutil.TreePathUtil;
import org.checkerframework.javacutil.TreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.VariableElement;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// TODO: Provide field initial values (e.g., use RHS type for static immutable fields, for initialization checks)
// TODO: pre and post conditions
public final class CFGAnalysisScanner extends TreePathScanner<Void, Void> implements EventListener {

    public static final Logger logger = LoggerFactory.getLogger(CFGAnalysisScanner.class);

    private static final Comparator<Tree> initializationOrder = Comparator.comparing(InitializationOrder::from);

    private final Context context;

    private final PluginOptions options;

    private final ProcessingEnvironment env;

    private final FlowContext flowContext;

    private final FlowAnalysis flowAnalysis;

    private final FlowTransfer flowTransfer;

    private final TypeSystems typeSystems;

    private final ASTAnalysisScanner astAnalysisScanner;

    // TODO: consider one viz per type system
    private final @Nullable CFGVisualizer<FlowValue, FlowStore, FlowTransfer> cfgVisualizer;

    private final IdentityHashMap<Tree, @Nullable FlowStore> capturedStores;

    private final Deque<ClassTree> enclosingClasses;

    private final IdentityHashMap<ClassTree, @Nullable FlowStore> initializationStores;

    private final IdentityHashMap<ClassTree, @Nullable FlowStore> initializationStaticStores;

    public static CFGAnalysisScanner instance(Context context) {
        CFGAnalysisScanner instance = context.get(CFGAnalysisScanner.class);
        if (instance == null) {
            instance = new CFGAnalysisScanner(context);
        }
        return instance;
    }

    private CFGAnalysisScanner(Context context) {
        this.context = context;
        this.options = PluginOptions.instance(context);
        this.env = JavacProcessingEnvironment.instance(context);
        this.flowContext = FlowContext.instance(context);
        this.flowAnalysis = FlowAnalysis.instance(context);
        this.flowTransfer = FlowTransfer.instance(context);
        this.typeSystems = TypeSystems.instance(context);
        this.astAnalysisScanner = ASTAnalysisScanner.instance(context);
        this.capturedStores = new IdentityHashMap<>();
        this.enclosingClasses = new ArrayDeque<>();
        this.initializationStores = new IdentityHashMap<>();
        this.initializationStaticStores = new IdentityHashMap<>();

        this.cfgVisualizer = createCFGVisualizer();

        EventManager eventManager = EventManager.instance(context);
        eventManager.register(this);

        context.put(CFGAnalysisScanner.class, this);
    }

    @Override
    public void finished(Event e) {
        if (e instanceof Event.NewAnalysisTask) {
            flowContext.reset();
            capturedStores.clear();
            Verify.verify(enclosingClasses.isEmpty());
            Verify.verify(initializationStores.isEmpty());
            Verify.verify(initializationStaticStores.isEmpty());
        }
        if (e == Event.SimpleEvent.FULL_ANALYSIS) {
            if (cfgVisualizer != null) {
                cfgVisualizer.shutdown();
            }
        }
    }

    @Override
    public Void scan(TreePath path, Void unused) {
        TreePath prevPath = flowContext.getTreePath();
        flowContext.setTreePath(path);
        try {
            return super.scan(path, unused);
        } finally {
            flowContext.setTreePath(prevPath);
        }
    }

    @Override
    public Void scan(Tree tree, Void unused) {
        if (tree == null) {
            return null;
        }

        TreePath prevPath = flowContext.getTreePath();
        flowContext.setTreePath(new TreePath(getCurrentPath(), tree));
        try {
            return super.scan(tree, unused);
        } finally {
            flowContext.setTreePath(prevPath);
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
        initializationStores.put(tree, capturedStores.get(tree));
        initializationStaticStores.put(tree, capturedStores.get(tree));
        enclosingClasses.push(tree);

        try {
            List<? extends Tree> members = tree.getMembers();
            if (!Comparators.isInOrder(members, initializationOrder)) {
                members = new ArrayList<>(tree.getMembers());
                members.sort(initializationOrder);
            }
            return scan(members, null);
        } finally {
            initializationStores.remove(tree);
            initializationStaticStores.remove(tree);
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
        ControlFlowGraph cfg = buildCFG(new TreePath(path, tree.getBody()), method);

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
        ControlFlowGraph cfg = buildCFG(getCurrentPath(), statement);

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
        ControlFlowGraph cfg = buildCFG(path, statement);

        process(tree, cfg);
        return super.visitBlock(tree, unused);
    }

    @Override
    public Void visitLambdaExpression(LambdaExpressionTree tree, Void unused) {
        TreePath path = getCurrentPath();
        MethodTree methodTree = TreePathUtil.enclosingMethod(path);
        ClassTree enclosingClass = enclosingClasses.peek();
        UnderlyingAST.CFGLambda lambda = new UnderlyingAST.CFGLambda(tree, enclosingClass, methodTree);
        ControlFlowGraph cfg = buildCFG(new TreePath(path, tree.getBody()), lambda);

        process(tree, cfg);
        return super.visitLambdaExpression(tree, unused);
    }

    private @Nullable CFGVisualizer<FlowValue, FlowStore, FlowTransfer> createCFGVisualizer() {
        if (options.getFlowOutDir() != null) {
            Map<String, Object> args = new HashMap<>(3);
            args.put("outdir", options.getFlowOutDir());
            args.put("verbose", options.isVerboseCfg());
            args.put("checkerName", "CFGChecker");

            CFGVisualizer<FlowValue, FlowStore, FlowTransfer> viz = new DOTCFGVisualizer<>();
            viz.init(args);
            return viz;
        }
        return null;
    }

    private ControlFlowGraph buildCFG(TreePath path, UnderlyingAST ast) {
        IdentityHashMap<Tree, TreePath> artificialTrees = new IdentityHashMap<>();
        ArtificialTreeHandler artificialTreeHandler = (artificialTree, currentPath) ->
                artificialTrees.put(artificialTree, currentPath);
        ControlFlowGraph cfg = CFGBuilder.build(
                path, ast, options.isAssertionEnabled(), !options.isAssertionEnabled(), env, artificialTreeHandler);

        flowContext.addArtificialTrees(artificialTrees);
        astAnalysisScanner.handleArtificialTrees(artificialTrees);

        return cfg;
    }

    private void process(Tree tree, ControlFlowGraph cfg) {
        flowContext.setCurrentCFG(cfg);
        flowContext.setFlowAnalysis(flowAnalysis);
        resolveTypes(tree, cfg);
        flowContext.setFlowAnalysis(null);

        visualizeCFG(cfg);
        performTypeChecks(cfg);

        flowContext.setCurrentCFG(null);
    }

    private void resolveTypes(Tree tree, ControlFlowGraph cfg) {
        IdentityHashMap<ClassTree, @Nullable FlowStore> useInitializationStore = null;
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
            default -> Verify.verify(
                    tree.getKind() == Tree.Kind.LAMBDA_EXPRESSION || tree.getKind() == Tree.Kind.METHOD,
                    "Unexpected tree to analyze: %s",
                    tree.getKind()
            );
        }

        FlowStore fixedInitStore;
        ClassTree enclosingClass = Verify.verifyNotNull(enclosingClasses.peek());
        if (useInitializationStore != null) {
            fixedInitStore = useInitializationStore.get(enclosingClass);
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
            useInitializationStore.put(enclosingClass, flowAnalysis.getRegularExitStore());
        }
    }

    private void visualizeCFG(ControlFlowGraph cfg) {
        if (cfgVisualizer != null) {
            cfgVisualizer.visualizeWithAction(cfg, cfg.getEntryBlock(), flowAnalysis);
        }
    }

    private void performTypeChecks(ControlFlowGraph cfg) {
        IdentityHashMap<Node, FlowValue> nodeValues = flowAnalysis.getNodeValues();

        for (Block block : cfg.getAllBlocks()) {
            TransferInput<FlowValue, FlowStore> input = flowAnalysis.getInput(block);
            Objects.requireNonNull(input);

            if (block.getType() == Block.BlockType.SPECIAL_BLOCK) {
                for (TypeSystem ts : typeSystems.get()) {
                    ts.getTypeChecker().checkSpecialBlock((SpecialBlock) block, input.getRegularStore());
                }
                continue;
            }

            for (Node node : block.getNodes()) {
                for (TypeSystem ts : typeSystems.get()) {
                    ts.getTypeChecker().checkNode(node, nodeValues);
                }
            }
            // Node lastNode = block.getLastNode();
            // if (lastNode != null) {
            //     Map<TransferInput<FlowValue, FlowStore>, IdentityHashMap<Node, TransferResult<FlowValue, FlowStore>>>
            //             cache = new IdentityHashMap<>();
            //     flowAnalysis.runAnalysisFor(lastNode,
            //             Analysis.BeforeOrAfter.AFTER,
            //             input,
            //             nodeValues,
            //             cache);
            //
            //     IdentityHashMap<Node, TransferResult<FlowValue, FlowStore>> nodeToResult =
            //             Objects.requireNonNull(cache.get(input));
            //     for (Node node : block.getNodes()) {
            //         for (TypeSystem ts : typeSystems.get()) {
            //             TransferResult<FlowValue, FlowStore> result = nodeToResult.get(node);
            //             ts.getTypeChecker().checkNode(node, result);
            //         }
            //     }
            // }
        }
    }
}
