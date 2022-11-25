package org.cfginference.core.flow;

import com.google.common.cache.CacheBuilder;
import com.sun.jdi.Value;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.TypeSystems;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.Analysis;
import org.checkerframework.dataflow.analysis.AnalysisResult;
import org.checkerframework.dataflow.analysis.ForwardAnalysisImpl;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.node.Node;

import javax.lang.model.type.TypeMirror;
import java.util.Set;

public final class FlowAnalysis extends ForwardAnalysisImpl<FlowValue, FlowStore, FlowTransfer> {

    private final FlowContext flowContext;

    private FlowAnalysis(Context context) {
        super(getMaxCountBeforeWidening(context));
        this.transferFunction = FlowTransfer.instance(context);
        this.flowContext = FlowContext.instance(context);

        context.put(FlowAnalysis.class, this);
    }

    // Do we want to expose this class?
    static FlowAnalysis instance(Context context) {
        FlowAnalysis instance = context.get(FlowAnalysis.class);
        if (instance == null) {
            instance = new FlowAnalysis(context);
        }
        return instance;
    }

    @Override
    protected boolean isIgnoredExceptionType(TypeMirror exceptionType) {
        // TODO: support this
        return false;
    }

    public @Nullable FlowStore getStoreBefore(Tree tree) {
        Set<Node> nodes = getNodesForTree(tree);
        if (nodes != null) {
            return getStoreBefore(nodes);
        }
        return null;
    }

    public FlowStore getStoreBefore(Set<Node> nodes) {
        FlowStore merge = null;
        for (Node node : nodes) {
            FlowStore s = getStoreBefore(node);
            if (merge == null) {
                merge = s;
            } else if (s != null) {
                merge = merge.leastUpperBound(s);
            }
        }
        return merge;
    }

    public FlowStore getStoreBefore(Node node) {
        Block block = node.getBlock();
        TransferInput<FlowValue, FlowStore> transferInput = getInput(block);
        if (transferInput == null) {
            return null;
        }

        return AnalysisResult.runAnalysisFor(
                node,
                BeforeOrAfter.BEFORE,
                transferInput,
                getNodeValues(),
                flowContext.getAnalysisCaches());
    }


    // TODO: does this make sense?
    private static int getMaxCountBeforeWidening(Context context) {
        return TypeSystems.instance(context).get().stream()
                .map(ts -> ts.getTransferFunction().maxCountBeforeWidening())
                .max(Integer::compareTo)
                .orElse(-1);
    }
}
