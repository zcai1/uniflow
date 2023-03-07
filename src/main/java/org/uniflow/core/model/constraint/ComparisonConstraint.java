package org.uniflow.core.model.constraint;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import com.sun.source.tree.Tree;
import org.uniflow.core.model.reporting.PluginError;
import org.uniflow.core.model.slot.ComparisonSlot;
import org.uniflow.core.model.slot.Slot;
import org.uniflow.core.model.util.serialization.Serializer;

/**
 * Represents constraints created for:
 *  (1) numerical comparison ("==", "!=", ">", ">=", "<", "<=")
 *  (2) comparison between references/null ("==", "!=")
 * <p>
 * In contrast, a {@link ComparableConstraint} describes two types are compatible,
 * i.e. one type can be cast to the other.
 */
@AutoValue
public abstract class ComparisonConstraint extends Constraint {

    public enum ComparisonOperation {
        EQUAL_TO("=="),
        NOT_EQUAL_TO("!="),
        GREATER_THAN(">"),
        GREATER_THAN_EQUAL(">="),
        LESS_THAN("<"),
        LESS_THAN_EQUAL("<=");

        // stores the symbol of the operation
        public final String opSymbol;

        ComparisonOperation(String opSymbol) {
            this.opSymbol = opSymbol;
        }

        public static ComparisonOperation fromTreeKind(Tree.Kind kind) {
            return switch (kind) {
                case EQUAL_TO -> EQUAL_TO;
                case NOT_EQUAL_TO -> NOT_EQUAL_TO;
                case GREATER_THAN -> GREATER_THAN;
                case GREATER_THAN_EQUAL -> GREATER_THAN_EQUAL;
                case LESS_THAN -> LESS_THAN;
                case LESS_THAN_EQUAL -> LESS_THAN_EQUAL;
                default -> throw new PluginError(
                        "Failed to find the corresponding ComparisonOperation for king %s", kind);
            };
        }
    }

    public abstract ComparisonOperation getOperation();

    public abstract Slot getLeft();

    public abstract Slot getRight();

    public abstract ComparisonSlot getResult();

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
