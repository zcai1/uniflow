package org.cfginference.core.flow;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.TypeSystems;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.AnalysisResult;
import org.checkerframework.dataflow.analysis.ForwardAnalysisImpl;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.block.Block;
import org.checkerframework.dataflow.cfg.node.Node;

import javax.lang.model.type.TypeMirror;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public final class FlowAnalysis extends ForwardAnalysisImpl<FlowValue, FlowStore, FlowTransfer> {

    private final FlowContext flowContext;

    /**
     * last node analyzed in the current block analysis
     */
    private @Nullable Node approxCurrentNode;

    private FlowAnalysis(Context context) {
        super(getMaxCountBeforeWidening(context));
        this.transferFunction = FlowTransfer.instance(context);
        this.flowContext = FlowContext.instance(context);
        this.approxCurrentNode = null;

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

    @Override
    public IdentityHashMap<Node, FlowValue> getNodeValues() {
        return new IdentityHashMap<>(super.getNodeValues());
    }

    @Override
    public @Nullable FlowValue getValue(Node n) {
        // copy of super.getValue(), but without the "assert !n.isLValue()" check.
        if (isRunning) {
            // we don't have a org.checkerframework.dataflow fact about the current node yet
            if (currentNode == null
                    || currentNode == n
                    || (currentTree != null && currentTree == n.getTree())) {
                return null;
            }
            if (!currentNode.getOperands().contains(n)
                    && !currentNode.getTransitiveOperands().contains(n)) {
                return null;
            }
            // fall through when the current node is not 'n', and 'n' is not a subnode.
        }
        return nodeValues.get(n);
    }

    public @Nullable FlowStore getStoreBefore(Tree tree) {
        if (isRunning()) {
            return flowContext.getFlowResult().getStoreBefore(tree);
        }

        Set<Node> nodes = getNodesForTree(tree);
        if (nodes != null) {
            return getStoreBefore(nodes);
        }
        return null;
    }

    @Override
    public void performAnalysisBlock(Block b) {
        try {
            super.performAnalysisBlock(b);
        } finally {
            // TODO: consider how to set approx node if block is empty, e.g., conditional block
            approxCurrentNode = null;
        }
    }

    @Override
    public FlowStore runAnalysisFor(Node node,
                                    BeforeOrAfter preOrPost,
                                    TransferInput<FlowValue, FlowStore> blockTransferInput,
                                    IdentityHashMap<Node, FlowValue> nodeValues,
                                    Map<TransferInput<FlowValue, FlowStore>, IdentityHashMap<Node, TransferResult<FlowValue, FlowStore>>> analysisCaches) {
        try {
            return super.runAnalysisFor(node, preOrPost, blockTransferInput, nodeValues, analysisCaches);
        } finally {
            approxCurrentNode = null;
        }
    }

    @Nullable Node getCurrentNode() {
        return currentNode;
    }

    @Nullable Node getApproxCurrentNode() {
        return approxCurrentNode;
    }

    @Override
    protected void setCurrentNode(@Nullable Node currentNode) {
        Node prevNode = getCurrentNode();
        if (prevNode != null) {
            approxCurrentNode = prevNode;
        }
        super.setCurrentNode(currentNode);
    }

    private FlowStore getStoreBefore(Set<Node> nodes) {
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

    private FlowStore getStoreBefore(Node node) {
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
