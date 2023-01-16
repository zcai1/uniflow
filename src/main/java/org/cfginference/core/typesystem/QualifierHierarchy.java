package org.cfginference.core.typesystem;

import org.cfginference.core.model.qualifier.Qualifier;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.lang.model.element.AnnotationMirror;
import java.util.Set;

public interface QualifierHierarchy {

    /**
     * Determines whether {@code qualifier} is in this hierarchy.
     * @param qualifier a qualifier to check
     * @return true if {@code qualifier} is in this hierarchy
     */
    boolean isInHierarchy(Qualifier qualifier);

    Qualifier getTopQualifier();

    Qualifier getBottomQualifier();

    @Nullable Qualifier getPolymorphicQualifier();

    boolean isPolymorphicQualifier(Qualifier qualifier);

    boolean isSubtype(Qualifier subQualifier, Qualifier superQualifier);

    Qualifier leastUpperBound(Qualifier qualifier1, Qualifier qualifier2);

    Qualifier greatestLowerBound(Qualifier qualifier1, Qualifier qualifier2);

    /**
     * Returns the number of iterations dataflow should perform before {@link
     * #widenedUpperBound(Qualifier, Qualifier)} is called or -1 if it should never be
     * called.
     *
     * @return the number of iterations dataflow should perform before {@link
     *     #widenedUpperBound(Qualifier, Qualifier)} is called or -1 if it should
     *     never be called.
     */
    default int numberOfIterationsBeforeWidening() {
        return -1;
    }

    /**
     * If the qualifier hierarchy has an infinite ascending chain, then the dataflow analysis might
     * never reach a fixed point. To prevent this, implement this method such that it returns an
     * upper bound for the two qualifiers that is a strict super type of the least upper bound. If
     * this method is implemented, also override {@link #numberOfIterationsBeforeWidening()} to
     * return a positive number.
     *
     * <p>{@code newQualifier} is newest qualifier dataflow computed for some expression and {@code
     * previousQualifier} is the qualifier dataflow computed on the last iteration.
     *
     * <p>If the qualifier hierarchy has no infinite ascending chain, returns the least upper bound
     * of the two Qualifiers.
     *
     * @param newQualifier new qualifier dataflow computed for some expression; must be in the same
     *     hierarchy as {@code previousQualifier}
     * @param previousQualifier the previous qualifier dataflow computed on the last iteration; must
     *     be in the same hierarchy as {@code previousQualifier}
     * @return an upper bound that is higher than the least upper bound of newQualifier and
     *     previousQualifier (or the lub if the qualifier hierarchy does not require this)
     */
    default Qualifier widenedUpperBound(Qualifier newQualifier, Qualifier previousQualifier) {
        return leastUpperBound(newQualifier, previousQualifier);
    }

    // Converts an annotation mirror to qualifier, or null if it's not supported
    @Nullable Qualifier getQualifier(AnnotationMirror annotationMirror);

    Set<Qualifier> getAllDefaultQualifiers();
}
