package org.cfginference.core.typesystem;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.flow.FlowContext;
import org.cfginference.core.flow.FlowStore;
import org.cfginference.core.flow.FlowValue;
import org.cfginference.core.model.element.QualifiedExecutableElement;
import org.cfginference.core.model.element.QualifiedVariableElement;
import org.cfginference.core.model.slot.ProductSlot;
import org.cfginference.core.model.type.QualifiedType;
import org.cfginference.util.AnnotationHelpers;
import org.cfginference.util.ElementHelpers;
import org.cfginference.util.TreeHelpers;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.ConditionalTransferResult;
import org.checkerframework.dataflow.analysis.RegularTransferResult;
import org.checkerframework.dataflow.analysis.TransferInput;
import org.checkerframework.dataflow.analysis.TransferResult;
import org.checkerframework.dataflow.cfg.UnderlyingAST;
import org.checkerframework.dataflow.cfg.node.AbstractNodeVisitor;
import org.checkerframework.dataflow.cfg.node.ArrayAccessNode;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.dataflow.cfg.node.CaseNode;
import org.checkerframework.dataflow.cfg.node.ClassNameNode;
import org.checkerframework.dataflow.cfg.node.ConditionalNotNode;
import org.checkerframework.dataflow.cfg.node.EqualToNode;
import org.checkerframework.dataflow.cfg.node.ExpressionStatementNode;
import org.checkerframework.dataflow.cfg.node.FieldAccessNode;
import org.checkerframework.dataflow.cfg.node.InstanceOfNode;
import org.checkerframework.dataflow.cfg.node.LambdaResultExpressionNode;
import org.checkerframework.dataflow.cfg.node.LocalVariableNode;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.node.NarrowingConversionNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.NotEqualNode;
import org.checkerframework.dataflow.cfg.node.ObjectCreationNode;
import org.checkerframework.dataflow.cfg.node.ReturnNode;
import org.checkerframework.dataflow.cfg.node.StringConversionNode;
import org.checkerframework.dataflow.cfg.node.SwitchExpressionNode;
import org.checkerframework.dataflow.cfg.node.TernaryExpressionNode;
import org.checkerframework.dataflow.cfg.node.ThisNode;
import org.checkerframework.dataflow.cfg.node.VariableDeclarationNode;
import org.checkerframework.dataflow.cfg.node.WideningConversionNode;
import org.checkerframework.dataflow.expression.ArrayAccess;
import org.checkerframework.dataflow.expression.ClassName;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.dataflow.expression.LocalVariable;
import org.checkerframework.dataflow.expression.MethodCall;
import org.checkerframework.dataflow.expression.ThisReference;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.util.NodeUtils;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BaseTypeSystemTransfer
        extends AbstractNodeVisitor<TransferResult<FlowValue, FlowStore>, TransferInput<FlowValue, FlowStore>> 
        implements TypeSystemTransfer {

    private static final Logger logger = LoggerFactory.getLogger(BaseTypeSystemTransfer.class);

    protected final Context context;

    protected final DeclarationTypeResolver declarationTypeResolver;

    protected final NodeTypeResolver nodeTypeResolver;

    protected final FlowContext flowContext;

    protected final Types types;

    public BaseTypeSystemTransfer(Context context,
                                  TypeSystem typeSystem) {
        this.context = context;
        this.declarationTypeResolver = typeSystem.getDeclarationTypeResolver();
        this.nodeTypeResolver = typeSystem.getNodeTypeResolver();
        this.flowContext = FlowContext.instance(context);
        this.types = JavacTypes.instance(context);
    }

    @Override
    public FlowStore initialStore(UnderlyingAST underlyingAST,
                                  List<LocalVariableNode> parameters,
                                  @Nullable FlowStore fixedInitialStore) {
        FlowStore initialStore = (fixedInitialStore != null)
                ? new FlowStore(fixedInitialStore)
                : new FlowStore(context);

        switch (underlyingAST.getKind()) {
            case LAMBDA -> initialStore((UnderlyingAST.CFGLambda) underlyingAST, parameters, initialStore);
            case METHOD -> initialStore((UnderlyingAST.CFGMethod) underlyingAST, parameters, initialStore);
        }
        return initialStore;
    }

    protected void initialStore(UnderlyingAST.CFGLambda underlyingAST,
                                List<LocalVariableNode> parameters,
                                FlowStore initialStore) {
        initialStore.clearValues(ClassName.class, ArrayAccess.class, MethodCall.class);

        for (LocalVariableNode localVar : parameters) {
            QualifiedVariableElement<ProductSlot> declaredType = declarationTypeResolver.getType(localVar.getElement());
            FlowValue value = new FlowValue(context, declaredType.getType());
            initialStore.replaceValue(localVar, value);
        }

        TreePath currentPath = flowContext.getTreePath();
        assert currentPath != null && currentPath.getLeaf() == underlyingAST.getLambdaTree();
        Element enclosingElement = TreeHelpers.findEnclosingElementForLambda(currentPath);
        addFinalLocalValues(initialStore, enclosingElement); // TODO: is this redundant?
    }

    protected void initialStore(UnderlyingAST.CFGMethod underlyingAST,
                                List<LocalVariableNode> parameters,
                                FlowStore initialStore) {
        for (LocalVariableNode localVar : parameters) {
            QualifiedVariableElement<ProductSlot> paramType = declarationTypeResolver.getType(localVar.getElement());
            FlowValue paramValue = new FlowValue(context, paramType.getType());
            initialStore.replaceValue(localVar, paramValue);
        }
        addFinalLocalValues(initialStore, TreeUtils.elementFromDeclaration(underlyingAST.getMethod()));

        ExecutableElement execElement = TreeUtils.elementFromDeclaration(underlyingAST.getMethod());
        if (ElementUtils.hasReceiver(execElement) && execElement.getKind() != ElementKind.CONSTRUCTOR) {
            // type of "this" in constructor can be determined by NodeTypeResolver
            QualifiedExecutableElement execType = declarationTypeResolver.getType(execElement);
            QualifiedType<ProductSlot> receiverType = execType.getReceiverType();
            FlowValue receiverValue = new FlowValue(context, receiverType);
            initialStore.replaceValue(new ThisReference(receiverType.getJavaType()), receiverValue);
        }

        // TODO: add precondition info
        // TODO: add more accurate field values (constructor vs non-constructor)
    }

    private void addFinalLocalValues(FlowStore store, Element enclosingElement) {
        for (Map.Entry<VariableElement, FlowValue> e : flowContext.getFlowResult().getFinalLocalValues().entrySet()) {
            VariableElement varElement = e.getKey();
            Element elementEnclosingVar = varElement.getEnclosingElement();

            if (ElementHelpers.isEnclosing(elementEnclosingVar, enclosingElement)) {
                store.replaceValue(new LocalVariable(varElement), e.getValue());
            }
        }
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitNode(Node n, TransferInput<FlowValue, FlowStore> input) {
        return defaultTransferResult(n, input, ResultType.BY_INPUT);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitFieldAccess(FieldAccessNode n,
                                                                 TransferInput<FlowValue, FlowStore> input) {
        return defaultTransferResult(n, input, ResultType.REGULAR);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitArrayAccess(ArrayAccessNode n,
                                                                 TransferInput<FlowValue, FlowStore> input) {
        return defaultTransferResult(n, input, ResultType.REGULAR);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitThis(ThisNode n, TransferInput<FlowValue, FlowStore> input) {
        return defaultTransferResult(n, input, ResultType.REGULAR);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitVariableDeclaration(VariableDeclarationNode n,
                                                                         TransferInput<FlowValue, FlowStore> input) {
        return defaultTransferResult(n, input, ResultType.REGULAR);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitLocalVariable(LocalVariableNode n,
                                                                   TransferInput<FlowValue, FlowStore> input) {
        return defaultTransferResult(n, input, ResultType.REGULAR);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitClassName(ClassNameNode n,
                                                               TransferInput<FlowValue, FlowStore> input) {
        return defaultTransferResult(n, input, ResultType.BY_INPUT);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitTernaryExpression(TernaryExpressionNode n,
                                                                       TransferInput<FlowValue, FlowStore> input) {
        return defaultTransferResult(n, input, ResultType.CONDITIONAL);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitSwitchExpressionNode(SwitchExpressionNode n,
                                                                          TransferInput<FlowValue, FlowStore> input) {
        return n.getSwitchExpressionVar().accept(this, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitCase(CaseNode n, TransferInput<FlowValue, FlowStore> input) {
        FlowStore store = input.getRegularStore();
        AssignmentNode assignment = n.getSwitchOperand();
        TransferResult<FlowValue, FlowStore> lubResult = null;

        for (Node caseOperand : n.getCaseOperands()) {
            TransferResult<FlowValue, FlowStore> result = new ConditionalTransferResult<>(
                            finishValue(null, store),
                            input.getThenStore().copy(),
                            input.getElseStore().copy(),
                            false);
            FlowValue caseValue = input.getValueOfSubNode(caseOperand);
            FlowValue switchValue = store.getValue(assignment.getTarget());

            result = refineForEqualTo(result, caseOperand, assignment.getExpression(), caseValue, switchValue, true);
            // Update value of switch temporary variable
            result = refineForEqualTo(result, caseOperand, assignment.getTarget(), caseValue, switchValue, true);

            if (lubResult == null) {
                lubResult = result;
            } else {
                FlowStore thenStore = lubResult.getThenStore().leastUpperBound(result.getThenStore());
                FlowStore elseStore = lubResult.getElseStore().leastUpperBound(result.getElseStore());
                lubResult =
                        new ConditionalTransferResult<>(
                                null,
                                thenStore,
                                elseStore,
                                lubResult.storeChanged() || result.storeChanged());
            }
        }
        return lubResult;
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitConditionalNot(ConditionalNotNode n,
                                                                    TransferInput<FlowValue, FlowStore> input) {
        return defaultTransferResult(n, input, ResultType.CONDITIONAL);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitEqualTo(EqualToNode n,
                                                             TransferInput<FlowValue, FlowStore> input) {
        TransferResult<FlowValue, FlowStore> res = super.visitEqualTo(n, input);

        Node leftNode = n.getLeftOperand();
        Node rightNode = n.getRightOperand();
        FlowValue leftValue = input.getValueOfSubNode(leftNode);
        FlowValue rightValue = input.getValueOfSubNode(rightNode);

        if (res.containsTwoStores()
                && (NodeUtils.isConstantBoolean(leftNode, false)
                    || NodeUtils.isConstantBoolean(rightNode, false))) {
            // swapping the two stores
            res = new ConditionalTransferResult<>(
                    res.getResultValue(),
                    res.getElseStore(),
                    res.getThenStore(),
                    res.storeChanged()
            );
        }

        // if annotations differ, use the one that is more precise for both
        // sides (and add it to the store if possible)
        res = refineForEqualTo(res, leftNode, rightNode, leftValue, rightValue, true);
        res = refineForEqualTo(res, rightNode, leftNode, rightValue, leftValue, true);
        return res;
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitNotEqual(NotEqualNode n,
                                                              TransferInput<FlowValue, FlowStore> input) {
        TransferResult<FlowValue, FlowStore> res = super.visitNotEqual(n, input);

        Node leftNode = n.getLeftOperand();
        Node rightNode = n.getRightOperand();
        FlowValue leftValue = input.getValueOfSubNode(leftNode);
        FlowValue rightValue = input.getValueOfSubNode(rightNode);

        if (res.containsTwoStores()
                && (NodeUtils.isConstantBoolean(leftNode, true)
                    || NodeUtils.isConstantBoolean(rightNode, true))) {
            // swapping the two stores
            res = new ConditionalTransferResult<>(
                    res.getResultValue(),
                    res.getElseStore(),
                    res.getThenStore(),
                    res.storeChanged()
            );
        }

        // if annotations differ, use the one that is more precise for both
        // sides (and add it to the store if possible)
        res = refineForEqualTo(res, leftNode, rightNode, leftValue, rightValue, false);
        res = refineForEqualTo(res, rightNode, leftNode, rightValue, leftValue, false);
        return res;
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitAssignment(AssignmentNode n,
                                                                TransferInput<FlowValue, FlowStore> input) {
        TransferResult<FlowValue, FlowStore> res = defaultTransferResult(n, input, ResultType.BY_INPUT);
        Node lhs = n.getTarget();
        Node rhs = n.getExpression();
        FlowValue resultValue = res.getResultValue();

        if (n.isSynthetic() && res.containsTwoStores()) {
            // This is a synthetic assignment node created for a ternary expression. In this case
            // the `then` and `else` store are not merged.
            FlowStore thenStore = res.getThenStore();
            FlowStore elseStore = res.getElseStore();
            processCommonAssignment(res, lhs, rhs, thenStore);
            processCommonAssignment(res, lhs, rhs, elseStore);
            return new ConditionalTransferResult<>(
                    finishValue(resultValue, thenStore, elseStore), thenStore, elseStore);
        } else {
            FlowStore store = res.getRegularStore();
            processCommonAssignment(res, lhs, rhs, store);
            return new RegularTransferResult<>(finishValue(resultValue, store), store);
        }
    }

    protected void processCommonAssignment(
            TransferResult<FlowValue, FlowStore> result,
            Node lhs,
            Node rhs,
            FlowStore store
    ) {
        // TODO: Should consider store changed?
        store.updateForAssignment(lhs, result.getResultValue());
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitReturn(ReturnNode n,
                                                            TransferInput<FlowValue, FlowStore> input) {
        return defaultTransferResult(n, input, ResultType.BY_INPUT);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitLambdaResultExpression(LambdaResultExpressionNode n,
                                                                            TransferInput<FlowValue, FlowStore> input) {
        return n.getResult().accept(this, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitObjectCreation(ObjectCreationNode n,
                                                                    TransferInput<FlowValue, FlowStore> input) {
        // TODO: process post-conditions
        return super.visitObjectCreation(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitMethodInvocation(MethodInvocationNode n,
                                                                      TransferInput<FlowValue, FlowStore> input) {
        // TODO: update store to invalidate some information
        // TODO: process pre and post-conditions
        return super.visitMethodInvocation(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitInstanceOf(InstanceOfNode n,
                                                                TransferInput<FlowValue, FlowStore> input) {
        TransferResult<FlowValue, FlowStore> res = defaultTransferResult(n, input, ResultType.CONDITIONAL);
        LocalVariableNode bindingVar = n.getBindingVariable();
        Node operand = n.getOperand();

        if (bindingVar != null) {
            FlowValue operandValue = input.getValueOfSubNode(operand);
            FlowValue bindingVarValue = input.getValueOfSubNode(bindingVar);

            // TODO: Treat it as equal to or assignment? Should be careful with
            //  where the binding variable is available.
            // if annotations differ, use the one that is more precise for both
            // sides (and add it to the store if possible)
            res = refineForEqualTo(res, operand, bindingVar, operandValue, bindingVarValue, true);
            res = refineForEqualTo(res, bindingVar, operand, bindingVarValue, operandValue, true);
        } else {
            if (res.containsTwoStores()) {
                res.getThenStore().insertValue(operand, res.getResultValue());
                res.getElseStore().insertValue(operand, res.getResultValue());
            } else {
                res.getRegularStore().insertValue(operand, res.getResultValue());
            }
        }
        return res;
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitWideningConversion(WideningConversionNode n,
                                                                        TransferInput<FlowValue, FlowStore> input) {
        return defaultTransferResult(n, input, ResultType.BY_INPUT);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitNarrowingConversion(NarrowingConversionNode n,
                                                                         TransferInput<FlowValue, FlowStore> input) {
        return defaultTransferResult(n, input, ResultType.BY_INPUT);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitStringConversion(StringConversionNode n,
                                                                      TransferInput<FlowValue, FlowStore> input) {
        return defaultTransferResult(n, input, ResultType.BY_INPUT);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitExpressionStatement(ExpressionStatementNode n,
                                                                         TransferInput<FlowValue, FlowStore> input) {
        return defaultTransferResult(n, input, ResultType.REGULAR);
    }

    /**
     * Takes a node, and either returns the node itself again (as a singleton list), or if the node
     * is an assignment node, returns the lhs and rhs (where splitAssignments is applied recursively
     * to the rhs -- that is, it is possible that the rhs does not appear in the result, but rather
     * its lhs and rhs do).
     *
     * @param node possibly an assignment node
     * @return a list containing all the right- and left-hand sides in the given assignment node; it
     *     contains just the node itself if it is not an assignment)
     */
    @SideEffectFree
    protected List<Node> splitAssignments(Node node) {
        if (node instanceof AssignmentNode assignment) {
            List<Node> result = new ArrayList<>(2);
            result.add(assignment.getTarget());
            result.addAll(splitAssignments(assignment.getExpression()));
            return result;
        } else {
            return Collections.singletonList(node);
        }
    }

    protected TransferResult<FlowValue, FlowStore> refineForEqualTo(
            TransferResult<FlowValue, FlowStore> initialResult,
            Node sourceNode,
            Node targetNode,
            @Nullable FlowValue sourceValue,
            @Nullable FlowValue targetValue,
            boolean equalTo
    ) {
        if (sourceValue == null || targetValue == null) {
            logger.debug(
                    "Skipping refinement for equal to expr: {} {} {}, LHS value: {}, RHS value: {}",
                    sourceNode,
                    equalTo ? "==" : "!=",
                    targetNode,
                    sourceValue,
                    targetValue
            );
            return initialResult;
        }
        if (sourceValue.equals(targetValue)) {
            return initialResult;
        }

        TransferResult<FlowValue, FlowStore> result = initialResult;
        for (Node targetPart : splitAssignments(targetNode)) {
            JavaExpression targetExpr = FlowStore.nodeToSupportExpression(targetPart);
            if (targetExpr == null || !targetExpr.isDeterministic(AnnotationHelpers.DEFAULT_ANNO_PROVIDER)) {
                continue;
            }

            FlowStore thenStore = result.getThenStore();
            FlowStore elseStore = result.getElseStore();
            FlowValue targetExprValue = (targetPart == targetNode)
                    ? targetValue
                    : (equalTo ? thenStore.getValue(targetExpr) : elseStore.getValue(targetExpr));
            if (targetExprValue == null) {
                // TODO: should we expect targetExpr to always have a non-null value in store?
                logger.error(
                        "Skipping refinement for {} {} {} because the LHS has no associated value",
                        targetPart,
                        equalTo ? "==" : "!=",
                        sourceNode
                );
                continue;
            }
            FlowValue refinedValue = new FlowValue(context,
                    nodeTypeResolver.refineType(targetExprValue.type, sourceValue.type));

            if (equalTo) {
                thenStore.insertValue(targetExpr, refinedValue);
            } else {
                elseStore.insertValue(targetExpr, refinedValue);
            }
            result = new ConditionalTransferResult<>(result.getResultValue(), thenStore, elseStore);
        }
        return result;
    }

    protected TransferResult<FlowValue, FlowStore> defaultTransferResult(Node n,
                                                                         TransferInput<FlowValue, FlowStore> input,
                                                                         ResultType resultType) {
        FlowValue value = createFlowValue(n, input);
        return createTransferResult(value, input, resultType);
    }

    @SideEffectFree
    protected TransferResult<FlowValue, FlowStore> createTransferResult(
            @Nullable FlowValue value,
            TransferInput<FlowValue, FlowStore> input,
            ResultType resultType
    ) {
        if (resultType == ResultType.CONDITIONAL
                || (resultType == ResultType.BY_INPUT && input.containsTwoStores())) {
            FlowStore thenStore = input.getThenStore();
            FlowStore elseStore = input.getElseStore();
            return new ConditionalTransferResult<>(
                    finishValue(value, thenStore, elseStore), thenStore, elseStore);
        } else {
            FlowStore store = input.getRegularStore();
            return new RegularTransferResult<>(finishValue(value, store), store);
        }
    }

    @SideEffectFree
    protected @Nullable FlowValue finishValue(@Nullable FlowValue value, FlowStore store) {
        return value;
    }

    protected @Nullable FlowValue finishValue(@Nullable FlowValue value, FlowStore thenStore, FlowStore elseStore) {
        return value;
    }

    protected @Nullable FlowValue createFlowValue(Node n, TransferInput<FlowValue, FlowStore> input) {
        QualifiedType<ProductSlot> nodeType = nodeTypeResolver.getType(n, input);
        return (nodeType == null) ? null : new FlowValue(context, nodeType);
    }

    private enum ResultType {
        REGULAR,
        CONDITIONAL,
        BY_INPUT
    }
}
