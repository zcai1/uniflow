package org.cfginference.core.solver.frontend;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.typesystem.QualifierHierarchy;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Special Lattice class for two qualifier type system.
 * 
 * @author jianchu
 *
 */
public class TwoQualifiersLattice extends Lattice {

    public TwoQualifiersLattice(Map<Qualifier, Collection<Qualifier>> subType,
                                Map<Qualifier, Collection<Qualifier>> superType,
                                Map<Qualifier, Collection<Qualifier>> incomparableType,
                                Set<? extends Qualifier> allTypes, Qualifier top, Qualifier bottom,
                                int numTypes,
                                QualifierHierarchy qualifierHierarchy) {
        super(subType, superType, incomparableType, allTypes, top, bottom, numTypes, null, qualifierHierarchy);
    }
}
