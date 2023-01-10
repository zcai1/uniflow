package org.cfginference.core.typesystem;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.PluginOptions;
import org.cfginference.core.event.Event;
import org.cfginference.core.event.EventListener;
import org.cfginference.core.event.EventManager;
import org.cfginference.core.flow.FlowContext;
import org.cfginference.core.flow.FlowStore;
import org.cfginference.core.flow.FlowValue;
import org.cfginference.core.flow.SlotQualifierHierarchy;
import org.cfginference.core.model.element.QualifiedExecutableElement;
import org.cfginference.core.model.element.QualifiedTypeElement;
import org.cfginference.core.model.location.LocationManager;
import org.cfginference.core.model.location.NodeLocation;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.reporting.PluginError;
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.PolymorphicInstanceSlot;
import org.cfginference.core.model.slot.ProductSlot;
import org.cfginference.core.model.slot.RefinementSlot;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.slot.SlotManager;
import org.cfginference.core.model.type.PrimaryQualifiedType;
import org.cfginference.core.model.type.QualifiedArrayType;
import org.cfginference.core.model.type.QualifiedExecutableType;
import org.cfginference.core.model.type.QualifiedIntersectionType;
import org.cfginference.core.model.type.QualifiedType;
import org.cfginference.core.model.type.QualifiedUnionType;
import org.cfginference.core.model.util.QualifiedTypeBuilder;
import org.cfginference.core.model.util.QualifiedTypeModifier;
import org.cfginference.util.ProductSlotUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.cfg.node.AbstractNodeVisitor;
import org.checkerframework.dataflow.cfg.node.ArrayAccessNode;
import org.checkerframework.dataflow.cfg.node.ArrayCreationNode;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.dataflow.cfg.node.ClassDeclarationNode;
import org.checkerframework.dataflow.cfg.node.FieldAccessNode;
import org.checkerframework.dataflow.cfg.node.InstanceOfNode;
import org.checkerframework.dataflow.cfg.node.LocalVariableNode;
import org.checkerframework.dataflow.cfg.node.MethodAccessNode;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.ObjectCreationNode;
import org.checkerframework.dataflow.cfg.node.ReturnNode;
import org.checkerframework.dataflow.cfg.node.ThisNode;
import org.checkerframework.dataflow.cfg.node.TypeCastNode;
import org.checkerframework.dataflow.cfg.node.VariableDeclarationNode;
import org.checkerframework.javacutil.TreePathUtil;
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

// TODO: verify generated type
// TODO: view-point adaptation
public class BaseNodeTypeResolver
        extends AbstractNodeVisitor<@Nullable QualifiedType<ProductSlot>, TransferInput<FlowValue, FlowStore>>
        implements NodeTypeResolver, EventListener {

    protected final Context context;

    protected final SlotManager slotManager;

    protected final PluginOptions options;

    protected final FlowContext flowContext;

    protected final LocationManager locationManager;

    protected final SlotQualifierHierarchy slotQualifierHierarchy;

    protected final DeclarationTypeResolver declarationTypeResolver;

    protected final Set<QualifierHierarchy> qualifierHierarchies;

    protected final Types types;

    protected final boolean inferenceMode;

    protected final IdentityHashMap<Node, QualifiedType<ProductSlot>> cache;

    protected final RefinementSlotTypeModifier refinementSlotTypeModifier;

    protected final PolySlotTypeModifier polySlotTypeModifier;

    protected final DefaultQualifiedTypeBuilder qualifiedTypeBuilder;

    public BaseNodeTypeResolver(Context context, TypeSystem typeSystem) {
        this.context = context;
        this.slotQualifierHierarchy = SlotQualifierHierarchy.instance(context);
        this.declarationTypeResolver = typeSystem.getDeclarationTypeResolver();
        this.qualifierHierarchies = ImmutableSet.copyOf(typeSystem.getQualifierHierarchies());
        this.slotManager = SlotManager.instance(context);
        this.locationManager = LocationManager.instance(context);
        this.options = PluginOptions.instance(context);
        this.flowContext = FlowContext.instance(context);
        this.types = JavacTypes.instance(context);
        this.inferenceMode = options.getMode().isInference();
        this.cache = new IdentityHashMap<>();

        this.refinementSlotTypeModifier = createRefinementSlotTypeModifier();
        this.polySlotTypeModifier = createPolySlotTypeModifier();
        this.qualifiedTypeBuilder = createDefaultQualifiedTypeBuilder();

        EventManager eventManager = EventManager.instance(context);
        eventManager.register(this);
    }

    @Override
    public void finished(Event e) {
        if (e instanceof Event.NewAnalysisTask) {
            cache.clear();
        }
    }

    @Override
    public @Nullable QualifiedType<ProductSlot> getType(Node node, TransferInput<FlowValue, FlowStore> input) {
        return node.accept(this, input);
    }

    @Override
    public QualifiedType<ProductSlot> visitNode(Node n, TransferInput<FlowValue, FlowStore> input) {
        return qualifiedTypeBuilder.visit(n.getType(), null);
    }

    @Override
    public QualifiedType<ProductSlot> visitVariableDeclaration(VariableDeclarationNode n,
                                                               TransferInput<FlowValue, FlowStore> input) {
        return declarationTypeResolver.getType(TreeUtils.elementFromDeclaration(n.getTree())).getType();
    }

    @Override
    public QualifiedType<ProductSlot> visitAssignment(AssignmentNode n,
                                                      TransferInput<FlowValue, FlowStore> input) {
        if (inferenceMode) {
            QualifiedType<ProductSlot> cachedResult = cache.get(n);
            if (cachedResult != null) {
                return cachedResult;
            }
        }

        QualifiedType<ProductSlot> lhsType = getLhsNodeType(n.getTarget());
        QualifiedType<ProductSlot> rhsType = input.getValueOfSubNode(n.getExpression()).type;

        QualifiedType<ProductSlot> refinedType;
        if (inferenceMode) {
            refinedType = refinementSlotTypeModifier.visit(lhsType, null);
            cache.put(n, refinedType);
        } else {
            refinedType = refineType(lhsType, rhsType);
        }
        return refinedType;
    }

    @Override
    public @Nullable QualifiedType<ProductSlot> visitReturn(ReturnNode n, TransferInput<FlowValue, FlowStore> input) {
        Node returnNode = n.getResult();
        if (returnNode == null) {
            return null;
        }
        return input.getValueOfSubNode(returnNode).type;
    }

    @Override
    public QualifiedType<ProductSlot> visitMethodAccess(MethodAccessNode n,
                                                        TransferInput<FlowValue, FlowStore> input) {
        QualifiedType<ProductSlot> cachedResult = cache.get(n);
        if (cachedResult != null) {
            return cachedResult;
        }

        QualifiedType<ProductSlot> type = declarationTypeResolver.getType(n.getMethod()).asType();

        try {
            type = polySlotTypeModifier.visit(type, null);
        } finally {
            polySlotTypeModifier.reset();
        }

        cache.put(n, type);
        return type;
    }

    @Override
    public QualifiedType<ProductSlot> visitMethodInvocation(MethodInvocationNode n,
                                                            TransferInput<FlowValue, FlowStore> input) {
        QualifiedExecutableType<ProductSlot> execType =
                (QualifiedExecutableType<ProductSlot>) input.getValueOfSubNode(n.getTarget()).type;
        return execType.getReturnType();
    }

    @Override
    public QualifiedType<ProductSlot> visitArrayCreation(ArrayCreationNode n,
                                                         TransferInput<FlowValue, FlowStore> input) {
        // TODO: infer qualifier from initializers
        // TODO: handle varargs
        return super.visitArrayCreation(n, input);
    }

    @Override
    public @Nullable QualifiedType<ProductSlot> visitArrayAccess(ArrayAccessNode n,
                                                                 TransferInput<FlowValue, FlowStore> input) {
        QualifiedArrayType<ProductSlot> arrayType =
                (QualifiedArrayType<ProductSlot>) input.getValueOfSubNode(n.getArray()).type;
        return arrayType.getComponentType();
    }

    @Override
    public QualifiedType<ProductSlot> visitTypeCast(TypeCastNode n,
                                                    TransferInput<FlowValue, FlowStore> input) {
        // TODO: take annotations from source
        return super.visitTypeCast(n, input);
    }

    @Override
    public QualifiedType<ProductSlot> visitInstanceOf(InstanceOfNode n, TransferInput<FlowValue, FlowStore> input) {
        // TODO: take annotations from source
        return super.visitInstanceOf(n, input);
    }

    @Override
    public QualifiedType<ProductSlot> visitLocalVariable(LocalVariableNode n,
                                                         TransferInput<FlowValue, FlowStore> input) {
        FlowValue existingValue = input.getRegularStore().getValue(n);
        if (existingValue != null) {
            return existingValue.type;
        }
        return declarationTypeResolver.getType(n.getElement()).getType();
    }

    @Override
    public QualifiedType<ProductSlot> visitFieldAccess(FieldAccessNode n,
                                                       TransferInput<FlowValue, FlowStore> input) {
        FlowValue existingValue = input.getRegularStore().getValue(n);
        if (existingValue != null) {
            return existingValue.type;
        }

        // determine if it's a receiver access in inner class constructor
        if (n.getFieldName().equals("this")) {
            MethodTree enclosingMethod =
                    Objects.requireNonNull(TreePathUtil.enclosingMethod(flowContext.getTreePath()));
            ExecutableElement methodElement = TreeUtils.elementFromDeclaration(enclosingMethod);

            if (methodElement.getKind() == ElementKind.CONSTRUCTOR) {
                QualifiedExecutableElement<ProductSlot> methodDeclType = declarationTypeResolver.getType(methodElement);
                QualifiedType<ProductSlot> receiverType = methodDeclType.getReceiverType();
                if (types.isSameType(n.getType(), receiverType.getJavaType())) {
                    return receiverType;
                }
            }
        }
        return declarationTypeResolver.getType(n.getElement()).getType();
    }

    @Override
    public QualifiedType<ProductSlot> visitThis(ThisNode n, TransferInput<FlowValue, FlowStore> input) {
        FlowValue existingValue = input.getRegularStore().getValue(n);
        if (existingValue != null) {
            return existingValue.type;
        }
        return super.visitThis(n, input);
    }

    @Override
    public QualifiedType<ProductSlot> visitObjectCreation(ObjectCreationNode n,
                                                          TransferInput<FlowValue, FlowStore> input) {
        // TODO: how to handle qualifiers?
        return super.visitObjectCreation(n, input);
    }

    @Override
    public QualifiedType<ProductSlot> visitClassDeclaration(ClassDeclarationNode n,
                                                            TransferInput<FlowValue, FlowStore> input) {
        // TODO: how to handle qualifiers?
        return super.visitClassDeclaration(n, input);
    }

    @Override
    public QualifiedType<ProductSlot> getLhsNodeType(Node n) {
        Preconditions.checkArgument(n instanceof FieldAccessNode
                || n instanceof LocalVariableNode
                || n instanceof ArrayAccessNode);

        QualifiedType<ProductSlot> lhsType;

        int arrayLevel = 0;
        Node outermostNode = n;
        while (outermostNode instanceof ArrayAccessNode arrayAccessLhs) {
            outermostNode = arrayAccessLhs.getArray();
            ++arrayLevel;
        }

        if (outermostNode instanceof FieldAccessNode fieldAccessLhs) {
            lhsType = declarationTypeResolver.getType(fieldAccessLhs.getElement()).getType();
        } else if (outermostNode instanceof LocalVariableNode localVariableLhs) {
            lhsType = declarationTypeResolver.getType(localVariableLhs.getElement()).getType();
        } else {
            assert arrayLevel > 0;
            // TODO: how to properly handle an array expr that is neither field access nor local var?
            throw new PluginError("Failed to find the lhs type for node %s", outermostNode);
        }

        while (arrayLevel > 0) {
            lhsType = ((QualifiedArrayType<ProductSlot>) lhsType).getComponentType();
            --arrayLevel;
        }
        return lhsType;
    }

    @Override
    public QualifiedType<ProductSlot> refineType(QualifiedType<ProductSlot> originalType,
                                                 QualifiedType<ProductSlot> maybePreciseType) {
        // case 1: both java types are the same, try to refine all slots
        if (originalType.structurallyEquals(types, maybePreciseType)) {
            return ProductSlotUtils.refine(context, originalType, maybePreciseType);
        }

        // TODO: consider cases where originalType/maybePreciseType is union/intersection type

        // case 2: the two java types are different, try to refine primary slots
        // TODO: should we try refining other slots?
        if (!(originalType instanceof PrimaryQualifiedType<ProductSlot> pqOriginalType
                && maybePreciseType instanceof PrimaryQualifiedType<ProductSlot> pqMaybePreciseType)) {
            return originalType;
        }

        Set<Slot> refinedSlots = new LinkedHashSet<>(qualifierHierarchies.size());
        boolean refined = false;
        for (QualifierHierarchy hierarchy : qualifierHierarchies) {
            Slot originalSlot = pqOriginalType.getQualifier().getSlotByHierarchy(hierarchy);
            Slot maybePreciseSlot = pqMaybePreciseType.getQualifier().getSlotByHierarchy(hierarchy);

            if (originalSlot == null || maybePreciseSlot == null) {
                throw new PluginError("Slot in %s hierarchy is missing in %s or %s",
                        hierarchy.getClass().getSimpleName(),
                        pqOriginalType,
                        pqMaybePreciseType);
            }

            // Should we refine variable slots?
            if (originalSlot instanceof ConstantSlot originalConst
                    && maybePreciseSlot instanceof ConstantSlot maybePreciseConst
                    && hierarchy.isSubtype(maybePreciseConst.getValue(), originalConst.getValue())
                    && !originalConst.equals(maybePreciseConst)) {
                refinedSlots.add(maybePreciseSlot);
                refined = true;
            } else {
                refinedSlots.add(originalSlot);
            }
        }

        if (!refined) {
            return pqOriginalType;
        }
        ProductSlot refinedProductSlot = slotManager.createProductSlot(refinedSlots);
        return pqOriginalType.withQualifier(refinedProductSlot);
    }

    protected RefinementSlotTypeModifier createRefinementSlotTypeModifier() {
        return new RefinementSlotTypeModifier();
    }

    protected PolySlotTypeModifier createPolySlotTypeModifier() {
        return new PolySlotTypeModifier();
    }

    protected DefaultQualifiedTypeBuilder createDefaultQualifiedTypeBuilder() {
        return new DefaultQualifiedTypeBuilder(context, qualifierHierarchies, declarationTypeResolver);
    }

    protected abstract class BaseTypeModifier<P> extends QualifiedTypeModifier<ProductSlot, ProductSlot, P> {

        @Override
        public QualifiedIntersectionType<ProductSlot> visitIntersection(QualifiedIntersectionType<ProductSlot> type, P p) {
            List<QualifiedType<ProductSlot>> bounds = visit(type.getBounds(), p);
            ProductSlot qualifier = ProductSlotUtils.mergePrimaries(context, bounds, qualifierHierarchies, true);
            return QualifiedIntersectionType.<ProductSlot>builder()
                    .setJavaType(type.getJavaType())
                    .setBounds(bounds)
                    .setQualifier(qualifier)
                    .build();
        }

        @Override
        public QualifiedUnionType<ProductSlot> visitUnion(QualifiedUnionType<ProductSlot> type, P p) {
            List<QualifiedType<ProductSlot>> alternatives = visit(type.getAlternatives(), p);
            ProductSlot qualifier = ProductSlotUtils.mergePrimaries(context, alternatives, qualifierHierarchies, true);
            return QualifiedUnionType.<ProductSlot>builder()
                    .setJavaType(type.getJavaType())
                    .setAlternatives(alternatives)
                    .setQualifier(qualifier)
                    .build();
        }
    }

    protected class RefinementSlotTypeModifier extends BaseTypeModifier<Void> {
        @Override
        protected ProductSlot defaultAction(PrimaryQualifiedType<ProductSlot> type, Void unused) {
            Map<QualifierHierarchy, RefinementSlot> refinementSlots = new LinkedHashMap<>();
            NodeLocation location = locationManager.getNodeLocation();
            for (QualifierHierarchy hierarchy : qualifierHierarchies) {
                Slot refinedSlot = type.getQualifier().getSlotByHierarchy(hierarchy);
                RefinementSlot refinementSlot = slotManager.createRefinementSlot(hierarchy, location, refinedSlot);
                refinementSlots.put(hierarchy, refinementSlot);
            }
            return slotManager.createProductSlot(refinementSlots);
        }
    }

    protected class PolySlotTypeModifier extends BaseTypeModifier<Void> {

        private Map<QualifierHierarchy, PolymorphicInstanceSlot> polySlots = new LinkedHashMap<>();

        private PolymorphicInstanceSlot getPolySlot(QualifierHierarchy hierarchy) {
            return polySlots.computeIfAbsent(
                    hierarchy,
                    h -> slotManager.createPolymorphicInstanceSlot(h, locationManager.getNodeLocation())
            );
        }

        public void reset() {
            polySlots.clear();
        }

        @Override
        protected ProductSlot defaultAction(PrimaryQualifiedType<ProductSlot> type, Void unused) {
            Map<QualifierHierarchy, Slot> result = new LinkedHashMap<>();
            boolean hasPoly = false;

            for (QualifierHierarchy hierarchy : qualifierHierarchies) {
                Slot slot = type.getQualifier().getSlotByHierarchy(hierarchy);
                if (slot instanceof ConstantSlot constantSlot) {
                    if (hierarchy.isPolymorphicQualifier(constantSlot.getValue())) {
                        hasPoly = true;
                        result.put(hierarchy, getPolySlot(hierarchy));
                    } else {
                        result.put(hierarchy, slot);
                    }
                }
            }

            if (!hasPoly) {
                return type.getQualifier();
            }
            return slotManager.createProductSlot(result);
        }
    }

    public static class DefaultQualifiedTypeBuilder extends QualifiedTypeBuilder<ProductSlot, Void> {

        protected final SlotManager slotManager;

        protected final Set<QualifierHierarchy> qualifierHierarchies;

        protected final DeclarationTypeResolver declarationTypeResolver;

        public DefaultQualifiedTypeBuilder(Context context,
                                           Set<QualifierHierarchy> qualifierHierarchies,
                                           DeclarationTypeResolver declarationTypeResolver) {
            this.slotManager = SlotManager.instance(context);
            this.qualifierHierarchies = qualifierHierarchies;
            this.declarationTypeResolver = declarationTypeResolver;
        }

        @Override
        protected ProductSlot getQualifier(TypeMirror type, Void unused) {
            if (type.getKind() == TypeKind.DECLARED) {
                DeclaredType declType = (DeclaredType) type;
                TypeElement typeElement = (TypeElement) declType.asElement();
                QualifiedTypeElement<ProductSlot> qualifiedDeclType = declarationTypeResolver.getType(typeElement);
                return qualifiedDeclType.getQualifier();
            }

            Map<QualifierHierarchy, Slot> defaultSlots = new LinkedHashMap<>();
            for (QualifierHierarchy hierarchy : qualifierHierarchies) {
                Qualifier top = hierarchy.getTopQualifier();
                ConstantSlot topConstSlot = slotManager.createConstantSlot(hierarchy, top);
                defaultSlots.put(hierarchy, topConstSlot);
            }
            return slotManager.createProductSlot(defaultSlots);
        }
    }
}
