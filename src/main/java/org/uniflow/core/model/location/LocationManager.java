package org.uniflow.core.model.location;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Context;
import org.uniflow.core.event.Event;
import org.uniflow.core.event.EventListener;
import org.uniflow.core.event.EventManager;
import org.uniflow.core.flow.FlowContext;
import org.uniflow.util.ASTPathUtils;
import org.uniflow.util.NodeUtils;
import org.checkerframework.dataflow.cfg.node.Node;
import scenelib.annotations.io.ASTRecord;

import javax.lang.model.element.TypeElement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class LocationManager implements EventListener {

    private final FlowContext flowContext;

    private final Map<Node, NodeLocation> nodeLocations;

    private LocationManager(Context context) {
        flowContext = FlowContext.instance(context);
        nodeLocations = new LinkedHashMap<>();

        EventManager eventManager = EventManager.instance(context);
        eventManager.register(this);

        context.put(LocationManager.class, this);
    }

    @Override
    public void finished(Event e) {
        if (e instanceof Event.NewAnalysisTask) {
            nodeLocations.clear();
        }
    }

    public static LocationManager instance(Context context) {
        LocationManager manager = context.get(LocationManager.class);
        if (manager == null) {
            manager = new LocationManager(context);
        }
        return manager;
    }

    public ASTLocation getASTLocation(ASTRecord astRecord, boolean insertable) {
        return new AutoValue_ASTLocation(insertable, ASTPathUtils.rootErased(astRecord));
    }

    public ASTLocation getASTLocation(Tree tree, boolean insertable) {
        ASTRecord astRecord = ASTPathUtils.getASTRecord(flowContext.getRoot(), tree);
        return getASTLocation(astRecord, insertable);
    }

    public NodeLocation getNodeLocation(Node node) {
        NodeLocation cachedResult = nodeLocations.get(node);
        if (cachedResult != null) {
            return cachedResult;
        }

        ASTRecord astRecord = null;
        Tree tree = NodeUtils.getRealSourceTree(flowContext, node);
        if (tree != null) {
            astRecord = ASTPathUtils.getASTRecord(flowContext.getRoot(), tree);
        }
        assert astRecord == null || astRecord.ast == null;

        NodeLocation result = new AutoValue_NodeLocation(node.getClass(), node.toString(), astRecord);
        nodeLocations.put(node, result);
        return result;
    }

    public NodeLocation getNodeLocation() {
        return getNodeLocation(Objects.requireNonNull(flowContext.getCurrentNodeApproximate()));
    }

    public ClassDeclLocation getClassDeclLocation(TypeElement typeElement) {
        return new AutoValue_ClassDeclLocation(((Symbol.ClassSymbol)typeElement).flatName().toString());
    }
}
