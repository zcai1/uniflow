package org.cfginference.core.flow;

import com.sun.tools.javac.util.Context;
import org.cfginference.core.TypeSystems;
import org.cfginference.core.typesystem.QualifierHierarchy;
import org.cfginference.core.typesystem.TypeSystem;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.ConditionalTransferResult;
import org.checkerframework.dataflow.analysis.ForwardTransferFunction;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.UnderlyingAST;
import org.checkerframework.dataflow.cfg.node.AbstractNodeVisitor;
import org.checkerframework.dataflow.cfg.node.LocalVariableNode;
import org.checkerframework.dataflow.cfg.node.Node;

import javax.lang.model.type.TypeMirror;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FlowTransfer
        extends AbstractNodeVisitor<TransferResult<FlowValue, FlowStore>, TransferInput<FlowValue, FlowStore>>
        implements ForwardTransferFunction<FlowValue, FlowStore> {

    private final Context context;

    private final TypeSystems typeSystems;

    private @Nullable FlowStore fixedInitialStore;

    private FlowTransfer(Context context) {
        this.context = context;
        this.typeSystems = TypeSystems.instance(context);
        assert typeSystems.get().size() > 0;

        context.put(FlowTransfer.class, this);
    }

    public static FlowTransfer instance(Context context) {
        FlowTransfer instance = context.get(FlowTransfer.class);
        if (instance == null) {
            instance = new FlowTransfer(context);
        }
        return instance;
    }

    public void setFixedInitialStore(@Nullable FlowStore fixedInitialStore) {
        this.fixedInitialStore = fixedInitialStore;
    }

    @Override
    public FlowStore initialStore(UnderlyingAST underlyingAST, List<LocalVariableNode> parameters) {
        FlowStore store = new FlowStore(context);
        for (TypeSystem ts : typeSystems.get()) {
            store.replace(
                    ts.getTransferFunction().initialStore(underlyingAST, parameters, fixedInitialStore),
                    ts.getQualifierHierarchies(),
                    true
            );
        }
        return store;
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitNode(
            Node n,
            TransferInput<FlowValue, FlowStore> input
    ) {
        TransferResult<FlowValue, FlowStore> r = null;
        int i = 0;
        int lastIndex = typeSystems.get().size() - 1;
        for (TypeSystem ts : typeSystems.get()) {
            TransferInput<FlowValue, FlowStore> thisInput = (i == lastIndex) ? input : input.copy();
            TransferResult<FlowValue, FlowStore> thisResult = n.accept(ts.getTransferFunction(), thisInput);
            r = (i == 0) ? thisResult : combineResults(r, thisResult, ts.getQualifierHierarchies());
            ++i;
        }
        return r;
    }

    // TODO: improve performance
    private TransferResult<FlowValue, FlowStore> combineResults(TransferResult<FlowValue, FlowStore> prevResult,
                                                                TransferResult<FlowValue, FlowStore> thisResult,
                                                                Set<QualifierHierarchy> thisHierarchies) {
        FlowValue newResultValue = FlowValue.replace(
                prevResult.getResultValue(),
                thisResult.getResultValue(),
                thisHierarchies
        );

        Map<TypeMirror, FlowStore> prevES = prevResult.getExceptionalStores();
        Map<TypeMirror, FlowStore> thisES = thisResult.getExceptionalStores();
        Map<TypeMirror, FlowStore> newExceptionalStores = null;

        if (prevES != null || thisES != null) {
            newExceptionalStores = new IdentityHashMap<>();

            if (prevES != null) {
                for (Map.Entry<TypeMirror, FlowStore> e : prevES.entrySet()) {
                    TypeMirror type = e.getKey();
                    FlowStore prevStore = e.getValue();
                    FlowStore thisStore = thisES == null ? null : thisES.get(type);
                    FlowStore newStore = prevStore.replace(thisStore, thisHierarchies, false);
                    newExceptionalStores.put(type, newStore);
                }
            }
            if (thisES != null) {
                for (Map.Entry<TypeMirror, FlowStore> e : thisES.entrySet()) {
                    TypeMirror type = e.getKey();
                    if (prevES == null || !prevES.containsKey(type)) {
                        newExceptionalStores.put(type, e.getValue());
                    }
                }
            }
        }

        boolean newStoreChanged = prevResult.storeChanged() || thisResult.storeChanged();

        // case 1: both don't have two stores
        if (!prevResult.containsTwoStores() && !thisResult.containsTwoStores()) {
            FlowStore newResultStore = prevResult.getRegularStore().replace(
                    thisResult.getRegularStore(),
                    thisHierarchies,
                    true
            );
            return new RegularTransferResult<>(newResultValue, newResultStore, newExceptionalStores, newStoreChanged);
        }

        // case 2: at least one has two stores
        FlowStore newThenStore = prevResult.getThenStore().replace(
                thisResult.getThenStore(),
                thisHierarchies,
                true
        );
        FlowStore newElseStore = prevResult.getElseStore().replace(
                thisResult.getElseStore(),
                thisHierarchies,
                true
        );
        return new ConditionalTransferResult<>(newResultValue,
                newThenStore,
                newElseStore,
                newExceptionalStores,
                newStoreChanged);
    }
}
