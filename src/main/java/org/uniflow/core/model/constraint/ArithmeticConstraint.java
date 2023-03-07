package org.uniflow.core.model.constraint;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import com.sun.source.tree.Tree;
import org.uniflow.core.model.reporting.PluginError;
import org.uniflow.core.model.slot.ArithmeticSlot;
import org.uniflow.core.model.slot.Slot;
import org.uniflow.core.model.util.serialization.Serializer;
import org.checkerframework.dataflow.cfg.node.BitwiseAndNode;
import org.checkerframework.dataflow.cfg.node.BitwiseOrNode;
import org.checkerframework.dataflow.cfg.node.BitwiseXorNode;
import org.checkerframework.dataflow.cfg.node.FloatingDivisionNode;
import org.checkerframework.dataflow.cfg.node.FloatingRemainderNode;
import org.checkerframework.dataflow.cfg.node.IntegerDivisionNode;
import org.checkerframework.dataflow.cfg.node.IntegerRemainderNode;
import org.checkerframework.dataflow.cfg.node.LeftShiftNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.NumericalAdditionNode;
import org.checkerframework.dataflow.cfg.node.NumericalMultiplicationNode;
import org.checkerframework.dataflow.cfg.node.NumericalSubtractionNode;
import org.checkerframework.dataflow.cfg.node.SignedRightShiftNode;
import org.checkerframework.dataflow.cfg.node.UnsignedRightShiftNode;

@AutoValue
public abstract class ArithmeticConstraint extends Constraint {

    public enum ArithmeticOperation {
        PLUS("+"),
        MINUS("-"),
        MULTIPLY("*"),
        DIVIDE("/"),
        REMAINDER("%"),
        LEFT_SHIFT("<<"),
        RIGHT_SHIFT(">>"),
        UNSIGNED_RIGHT_SHIFT(">>>"),
        AND("&"),
        OR("|"),
        XOR("^");

        // stores the symbol of the operation
        public final String opSymbol;

        ArithmeticOperation(String opSymbol) {
            this.opSymbol = opSymbol;
        }

        public static ArithmeticOperation fromTreeKind(Tree.Kind kind) {
            return switch (kind) {
                case PLUS, PLUS_ASSIGNMENT -> PLUS;
                case MINUS, MINUS_ASSIGNMENT -> MINUS;
                case MULTIPLY, MULTIPLY_ASSIGNMENT -> MULTIPLY;
                case DIVIDE, DIVIDE_ASSIGNMENT -> DIVIDE;
                case REMAINDER, REMAINDER_ASSIGNMENT -> REMAINDER;
                case LEFT_SHIFT, LEFT_SHIFT_ASSIGNMENT -> LEFT_SHIFT;
                case RIGHT_SHIFT, RIGHT_SHIFT_ASSIGNMENT -> RIGHT_SHIFT;
                case UNSIGNED_RIGHT_SHIFT, UNSIGNED_RIGHT_SHIFT_ASSIGNMENT -> UNSIGNED_RIGHT_SHIFT;
                case AND, AND_ASSIGNMENT -> AND;
                case OR, OR_ASSIGNMENT -> OR;
                case XOR, XOR_ASSIGNMENT -> XOR;
                default ->
                        throw new PluginError(
                                "Failed to find the corresponding ArithmeticOperation for king %s", kind);
            };
        }

        public static ArithmeticOperation fromNode(Node node) {
            if (node instanceof NumericalAdditionNode) {
                return PLUS;
            } else if (node instanceof NumericalSubtractionNode) {
                return MINUS;
            } else if (node instanceof NumericalMultiplicationNode) {
                return MULTIPLY;
            } else if (node instanceof IntegerDivisionNode || node instanceof FloatingDivisionNode) {
                return DIVIDE;
            } else if (node instanceof IntegerRemainderNode || node instanceof FloatingRemainderNode) {
                return REMAINDER;
            } else if (node instanceof LeftShiftNode) {
                return LEFT_SHIFT;
            } else if (node instanceof SignedRightShiftNode) {
                return RIGHT_SHIFT;
            } else if (node instanceof UnsignedRightShiftNode) {
                return UNSIGNED_RIGHT_SHIFT;
            } else if (node instanceof BitwiseAndNode) {
                return AND;
            } else if (node instanceof BitwiseOrNode) {
                return OR;
            } else if (node instanceof BitwiseXorNode) {
                return XOR;
            }

            throw new PluginError("Failed to find the corresponding ArithmeticOperation for node %s", node);
        }
    }

    public abstract ArithmeticOperation getOperation();

    public abstract Slot getLeft();

    public abstract Slot getRight();

    public abstract ArithmeticSlot getResult();

    @Override
    @Memoized
    public ImmutableList<Slot> getSlots() {
        return ImmutableList.of(getLeft(), getRight(), getResult());
    }

    @Override
    public final <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
