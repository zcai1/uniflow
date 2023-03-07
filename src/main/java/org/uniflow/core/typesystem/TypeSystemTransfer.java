package org.uniflow.core.typesystem;

import org.uniflow.core.flow.FlowStore;
import org.uniflow.core.flow.FlowValue;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.ForwardTransferFunction;
import org.checkerframework.dataflow.cfg.UnderlyingAST;
import org.checkerframework.dataflow.cfg.node.LocalVariableNode;

import java.util.List;

public interface TypeSystemTransfer extends ForwardTransferFunction<FlowValue, FlowStore> {
    default int maxCountBeforeWidening() {
        return -1;
    }

    FlowStore initialStore(UnderlyingAST underlyingAST,
                           List<LocalVariableNode> parameters,
                           @Nullable FlowStore fixedInitialStore);

    @Override
    default FlowStore initialStore(UnderlyingAST underlyingAST, List<LocalVariableNode> parameters) {
        return initialStore(underlyingAST, parameters, null);
    }
}
