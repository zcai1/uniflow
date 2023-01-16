package org.cfginference.core.solver.frontend;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.typesystem.QualifierHierarchy;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Lattice class pre-cache necessary qualifier information from qualifier hierarchy for
 * constraint solving.
 * <p>
 * It is convenient to get all subtypes and supertypes of a specific type
 * qualifier, all type qualifier, and bottom and top qualifiers from an instance
 * of this class.
 *
 * @author jianchu
 *
 */
public class Lattice {

    /**
     * subType maps each type qualifier to its sub types.
     */
    public final Map<Qualifier, Collection<Qualifier>> subType;

    /**
     * superType maps each type qualifier to its super types.
     */
    public final Map<Qualifier, Collection<Qualifier>> superType;

    /**
     * incomparableType maps each type qualifier to its incomparable types.
     */
    public final Map<Qualifier, Collection<Qualifier>> incomparableType;

    /**
     * All type qualifiers in underling type system.
     */
    public final Set<? extends Qualifier> allTypes;

    /**
     * Top qualifier of underling type system.
     */
    public final Qualifier top;

    /**
     * Bottom type qualifier of underling type system.
     */
    public final Qualifier bottom;

    /**
     * Number of type qualifier in underling type system.
     */
    public final int numTypes;

    /**
     * All concrete qualifiers information that collected from the program
     * CF Inference running on.
     * This field is useful for type systems that has a dynamic number
     * of type qualifiers.
     */
    public final @Nullable Collection<Qualifier> allQualifiers;

    /**
     * Underlying qualifier hierarchy that this lattice built based on.
     * This field is nullable, it will be null if this lattice doesn't built based on
     * a real qualifier hierarchy. (E.g. TwoQualifierLattice).
     */
    private final QualifierHierarchy underlyingQualifierHierarchy;

    public Lattice(Map<Qualifier, Collection<Qualifier>> subType,
                   Map<Qualifier, Collection<Qualifier>> superType,
                   Map<Qualifier, Collection<Qualifier>> incomparableType,
                   Set<? extends Qualifier> allTypes,
                   Qualifier top,
                   Qualifier bottom,
                   int numTypes,
                   @Nullable Collection<Qualifier> runtimeQuals,
                   QualifierHierarchy qualifierHierarchy) {
        this.subType = Collections.unmodifiableMap(subType);
        this.superType = Collections.unmodifiableMap(superType);
        this.incomparableType = Collections.unmodifiableMap(incomparableType);
        this.allTypes = Collections.unmodifiableSet(allTypes);
        this.top = top;
        this.bottom = bottom;
        this.numTypes = numTypes;
        this.underlyingQualifierHierarchy = qualifierHierarchy;
        this.allQualifiers = runtimeQuals;
    }

    public boolean isSubtype(Qualifier q1, Qualifier q2) {
        return underlyingQualifierHierarchy.isSubtype(q1, q2);
    }
}
