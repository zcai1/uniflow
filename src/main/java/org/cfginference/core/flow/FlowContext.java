package org.cfginference.core.flow;

import com.google.common.base.Verify;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.PluginOptions;
import org.cfginference.core.event.Event;
import org.cfginference.core.event.EventListener;
import org.cfginference.core.event.EventManager;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.AnalysisResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.expression.FieldAccess;
import org.checkerframework.javacutil.CollectionUtils;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FlowContext implements EventListener, ArtificialTreeHandler {

    private final Map<Tree, TreePath> artificialTrees;

    private final Map<FieldAccess, FlowValue> immutableFieldValues;

    private final Map<TransferInput<FlowValue, FlowStore>,
            IdentityHashMap<Node, TransferResult<FlowValue, FlowStore>>> analysisCaches;

    private AnalysisResult<FlowValue, FlowStore> flowResult;

    public static FlowContext instance(Context context) {
        FlowContext instance = context.get(FlowContext.class);
        if (instance == null) {
            instance = new FlowContext(context);
        }
        return instance;
    }

    private FlowContext(Context context) {
        PluginOptions options = PluginOptions.instance(context);
        artificialTrees = new LinkedHashMap<>();
        immutableFieldValues = new LinkedHashMap<>();
        analysisCaches = CollectionUtils.createLRUCache(options.getCacheSize());
        flowResult = new AnalysisResult<>(analysisCaches);

        EventManager eventManager = EventManager.instance(context);
        eventManager.register(this);

        context.put(FlowContext.class, this);
    }

    private void reset() {
        analysisCaches.clear();
        artificialTrees.clear();
        immutableFieldValues.clear();
        flowResult = new AnalysisResult<>(analysisCaches);
    }

    @Override
    public void finished(Event e) {
        if (e instanceof Event.NewAnalysisTask) {
            reset();
        }
    }

    @Override
    public void handleArtificialTree(Tree artificialTree, TreePath currentPath) {
        artificialTrees.put(artificialTree, currentPath);
    }

    public boolean isArtificialTree(Tree tree) {
        return artificialTrees.containsKey(tree);
    }

    public @Nullable TreePath getArtificialTreeParent(Tree tree) {
        return artificialTrees.get(tree);
    }

    public Map<FieldAccess, FlowValue> getImmutableFieldValues() {
        return Collections.unmodifiableMap(immutableFieldValues);
    }

    public void addImmutableFieldValue(FieldAccess field, FlowValue value) {
        immutableFieldValues.put(field, value);
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
}
