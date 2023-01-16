package org.cfginference.core.solver.frontend;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.typesystem.QualifierHierarchy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class LatticeBuilder {

    /**
     * subType maps each type qualifier to its sub types.
     */
    private final Map<Qualifier, Collection<Qualifier>> subType;

    /**
     * superType maps each type qualifier to its super types.
     */
    private final Map<Qualifier, Collection<Qualifier>> superType;

    /**
     * incomparableType maps each type qualifier to its incomparable types.
     */
    private final Map<Qualifier, Collection<Qualifier>> incomparableType;

    /**
     * All type qualifiers in underling type system.
     */
    // TODO: Does the order matter?
    private Set<Qualifier> allTypes;

    /**
     * Top qualifier of underling type system.
     */
    private Qualifier top;

    /**
     * Bottom type qualifier of underling type system.
     */
    private Qualifier bottom;

    /**
     * Number of type qualifier in underling type system.
     */
    private int numTypes;

    /**
     * All concrete qualifiers extracted from slots collected from the program
     * that CF Inference running on.
     * This field is useful for type systems that has a dynamic number
     * of type qualifiers.
     */
    public final Collection<Qualifier> allQualifiers;

    public LatticeBuilder() {
        subType = new LinkedHashMap<>();
        superType = new LinkedHashMap<>();
        incomparableType = new LinkedHashMap<>();
        allQualifiers = new LinkedHashSet<>();
    }

    /**
     * Build a normal lattice with all fields configured.
     *
     * @param qualifierHierarchy of underling type system.
     * @return a new Lattice instance.
     */
    public Lattice buildLattice(QualifierHierarchy qualifierHierarchy, Collection<Slot> slots) {
        clear();

        top = qualifierHierarchy.getTopQualifier();
        bottom = qualifierHierarchy.getBottomQualifier();

        allTypes = qualifierHierarchy.getAllDefaultQualifiers();
        numTypes = allTypes.size();

        // Calculate subtypes map and supertypes map
        for (Qualifier i : allTypes) {
            Set<Qualifier> subtypeOfi = new LinkedHashSet<>();
            Set<Qualifier> supertypeOfi = new LinkedHashSet<>();
            for (Qualifier j : allTypes) {
                if (qualifierHierarchy.isSubtype(j, i)) {
                    subtypeOfi.add(j);
                }
                if (qualifierHierarchy.isSubtype(i, j)) {
                    supertypeOfi.add(j);
                }
            }
            subType.put(i, subtypeOfi);
            superType.put(i, supertypeOfi);
        }

        // Calculate incomparable types map
        for (Qualifier i : allTypes) {
            Set<Qualifier> incomparableOfi = new LinkedHashSet<>();
            for (Qualifier j : allTypes) {
                if (!subType.get(i).contains(j) && !subType.get(j).contains(i)) {
                    incomparableOfi.add(j);
                }
            }
            if (!incomparableOfi.isEmpty()) {
                incomparableType.put(i, incomparableOfi);
            }
        }

        collectConstantQualifiers(slots);

        return new Lattice(subType, superType, incomparableType, allTypes, top,
                bottom, numTypes, allQualifiers, qualifierHierarchy);
    }

    /**
     * Build a two-qualifier lattice with all fields configured.
     * 
     * @param top type qualifier of underling type system.
     * @param bottom type qualifier of underling type system.
     * @return a new TwoQualifiersLattice instance.
     */
    public TwoQualifiersLattice buildTwoTypeLattice(QualifierHierarchy qualifierHierarchy,
                                                    Qualifier top, Qualifier bottom) {
        clear();

        Set<Qualifier> tempSet = new LinkedHashSet<>();
        tempSet.add(top);
        tempSet.add(bottom);
        allTypes = Collections.unmodifiableSet(tempSet);
        this.top = top;
        this.bottom = bottom;
        numTypes = 2;

        // Calculate subertypes map and supertypes map.
        Set<Qualifier> topSet = new LinkedHashSet<>();
        Set<Qualifier> bottomSet = new LinkedHashSet<>();
        topSet.add(top);
        bottomSet.add(bottom);
        subType.put(top, allTypes);
        superType.put(top, topSet);
        subType.put(bottom, bottomSet);
        superType.put(bottom, allTypes);

        return new TwoQualifiersLattice(subType, superType, incomparableType,
                allTypes, top, bottom, numTypes, qualifierHierarchy);
    }

    /**
     * Clear all fields. Will be called when build a new lattice to make sure
     * the old values are gone.
     */
    private void clear() {
        allQualifiers.clear();
        this.subType.clear();
        this.superType.clear();
        this.incomparableType.clear();
        allTypes = null;
        top = null;
        bottom = null;
        numTypes = 0;
    }

    /**
     * Extract annotation mirrors in constant slots of a given collection of slots.
     * @param slots a collection of slots.
     */
    private void collectConstantQualifiers(Collection<Slot> slots) {
           for(Slot slot : slots) {
               if (slot instanceof ConstantSlot) {
                   allQualifiers.add(((ConstantSlot) slot).getValue());
               }
           }
       }
}
