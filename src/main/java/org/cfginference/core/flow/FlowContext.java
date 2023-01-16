package org.cfginference.core.flow;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.PluginOptions;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.AnalysisResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.javacutil.CollectionUtils;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

public final class FlowContext {

    private final GeneralContext generalContext;

    private final Map<Tree, TreePath> artificialTrees;

    private final Map<TransferInput<FlowValue, FlowStore>,
            IdentityHashMap<Node, TransferResult<FlowValue, FlowStore>>> analysisCaches;

    private AnalysisResult<FlowValue, FlowStore> flowResult;

    private @Nullable ControlFlowGraph cfg;

    private @Nullable TreePath treePath;

    // Note: use a setter for this field to avoid cyclic dependency in constructor
    private @Nullable FlowAnalysis flowAnalysis;

    public static FlowContext instance(Context context) {
        FlowContext instance = context.get(FlowContext.class);
        if (instance == null) {
            instance = new FlowContext(context);
        }
        return instance;
    }

    private FlowContext(Context context) {
        PluginOptions options = PluginOptions.instance(context);
        generalContext = GeneralContext.instance(context);
        artificialTrees = new IdentityHashMap<>();
        analysisCaches = CollectionUtils.createLRUCache(options.getCacheSize());
        flowResult = new AnalysisResult<>(analysisCaches);

        context.put(FlowContext.class, this);
    }

    void reset() {
        analysisCaches.clear();
        artificialTrees.clear();
        flowResult = new AnalysisResult<>(analysisCaches);
        treePath = null;
    }

    void addArtificialTrees(Map<Tree, TreePath> treeToPath) {
        artificialTrees.putAll(treeToPath);
    }

    Map<Tree, TreePath> getArtificialTrees() {
        return Collections.unmodifiableMap(artificialTrees);
    }

    public boolean isArtificialTree(Tree tree) {
        return artificialTrees.containsKey(tree);
    }

    public @Nullable TreePath getPathToArtificialTreeParent(Tree tree) {
        return artificialTrees.get(tree);
    }

    public Map<TransferInput<FlowValue, FlowStore>,
            IdentityHashMap<Node, TransferResult<FlowValue, FlowStore>>> getAnalysisCaches() {
        return analysisCaches;
    }

    public AnalysisResult<FlowValue, FlowStore> getFlowResult() {
        return flowResult;
    }

    public void combineResult(AnalysisResult<FlowValue, FlowStore> otherResult) {
        flowResult.combine(otherResult);
    }

    public @Nullable TreePath getTreePath() {
        return treePath;
    }

    public void setTreePath(@Nullable TreePath treePath) {
        this.treePath = treePath;
    }

    public CompilationUnitTree getRoot() {
        return generalContext.getRoot();
    }

    public void setFlowAnalysis(FlowAnalysis flowAnalysis) {
        this.flowAnalysis = flowAnalysis;
    }

    public @Nullable Node getCurrentNodeInTransfer() {
        if (flowAnalysis != null) {
            return flowAnalysis.getCurrentNode();
        }
        return null;
    }

    public @Nullable Node getCurrentNodeApproximate() {
        if (flowAnalysis != null) {
            Node node = flowAnalysis.getCurrentNode();
            if (node == null) {
                node = flowAnalysis.getApproxCurrentNode();
            }
            return node;
        }
        return null;
    }

    public @Nullable ControlFlowGraph getCurrentCFG() {
        return cfg;
    }

    void setCurrentCFG(ControlFlowGraph cfg) {
        this.cfg = cfg;
    }
}
