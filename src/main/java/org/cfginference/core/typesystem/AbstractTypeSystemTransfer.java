package org.cfginference.core.typesystem;

import org.cfginference.core.flow.FlowStore;
import org.cfginference.core.flow.FlowValue;
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
import org.checkerframework.dataflow.cfg.node.MethodAccessNode;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.node.NarrowingConversionNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.NotEqualNode;
import org.checkerframework.dataflow.cfg.node.ObjectCreationNode;
import org.checkerframework.dataflow.cfg.node.ReturnNode;
import org.checkerframework.dataflow.cfg.node.StringConcatenateNode;
import org.checkerframework.dataflow.cfg.node.SwitchExpressionNode;
import org.checkerframework.dataflow.cfg.node.TernaryExpressionNode;
import org.checkerframework.dataflow.cfg.node.ThisNode;
import org.checkerframework.dataflow.cfg.node.VariableDeclarationNode;
import org.checkerframework.dataflow.cfg.node.WideningConversionNode;

import java.util.List;

public class AbstractTypeSystemTransfer
        extends AbstractNodeVisitor<TransferResult<FlowValue, FlowStore>, TransferInput<FlowValue, FlowStore>> 
        implements TypeSystemTransfer {

    @Override
    public FlowStore initialStore(UnderlyingAST underlyingAST,
                                  List<LocalVariableNode> parameters,
                                  @Nullable FlowStore fixedInitialStore) {
        return null;
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitNode(Node n, TransferInput<FlowValue, FlowStore> input) {
        return null;
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitFieldAccess(FieldAccessNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitFieldAccess(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitMethodAccess(MethodAccessNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitMethodAccess(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitArrayAccess(ArrayAccessNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitArrayAccess(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitThis(ThisNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitThis(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitVariableDeclaration(VariableDeclarationNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitVariableDeclaration(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitLocalVariable(LocalVariableNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitLocalVariable(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitClassName(ClassNameNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitClassName(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitTernaryExpression(TernaryExpressionNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitTernaryExpression(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitSwitchExpressionNode(SwitchExpressionNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitSwitchExpressionNode(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitCase(CaseNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitCase(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitConditionalNot(ConditionalNotNode n, TransferInput<FlowValue, FlowStore> input) {
        TransferResult<FlowValue, FlowStore> result = super.visitConditionalNot(n, input);
        FlowStore thenStore = result.getThenStore();
        FlowStore elseStore = result.getElseStore();
        return new ConditionalTransferResult<>(result.getResultValue(), elseStore, thenStore);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitEqualTo(EqualToNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitEqualTo(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitNotEqual(NotEqualNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitNotEqual(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitAssignment(AssignmentNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitAssignment(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitReturn(ReturnNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitReturn(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitLambdaResultExpression(LambdaResultExpressionNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitLambdaResultExpression(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitObjectCreation(ObjectCreationNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitObjectCreation(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitMethodInvocation(MethodInvocationNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitMethodInvocation(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitInstanceOf(InstanceOfNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitInstanceOf(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitWideningConversion(WideningConversionNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitWideningConversion(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitNarrowingConversion(NarrowingConversionNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitNarrowingConversion(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitStringConcatenate(StringConcatenateNode n, TransferInput<FlowValue, FlowStore> input) {
        return super.visitStringConcatenate(n, input);
    }

    @Override
    public TransferResult<FlowValue, FlowStore> visitExpressionStatement(ExpressionStatementNode n, TransferInput<FlowValue, FlowStore> input) {
        FlowStore store = input.getRegularStore();
        return new RegularTransferResult<>(finishValue(null, store), store);
    }

    protected @Nullable FlowValue finishValue(@Nullable FlowValue value, FlowStore store) {
        return value;
    }
}
