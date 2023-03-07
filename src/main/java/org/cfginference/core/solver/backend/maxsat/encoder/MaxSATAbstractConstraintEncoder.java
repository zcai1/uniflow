package org.cfginference.core.solver.backend.maxsat.encoder;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.solver.backend.encoder.AbstractConstraintEncoder;
import org.cfginference.core.solver.backend.maxsat.VectorUtils;
import org.cfginference.core.solver.frontend.Lattice;
import org.sat4j.core.VecInt;

import java.util.Map;

/**
 * Abstract base class for every MaxSAT constraint encoders.
 */
public abstract class MaxSATAbstractConstraintEncoder extends AbstractConstraintEncoder<VecInt[]> {

    private static VecInt[] EMPTY_CLAUSE = new VecInt[0];

    // Any contradictory two clauses can be used to model contradiction. But here 1,-1 are chosen because it's simple and
    // doesn't collides with real variable id 1 since 1 is almost always reserved for ConstantSlot(because every type system must
    // have at least one real qualifier). The only exception to this assumption is when storeConstants is false when initializing
    // DefaultSlotManager, but right now all the creation of DefaultSlotManager stores constants.
    private static VecInt[] CONTRADICTORY_CLAUSES = new VecInt[]{VectorUtils.asVec(1), VectorUtils.asVec(-1)};

    /**
     * A map shared by every MaxSAT constraint encoder.
     * See {@link checkers.inference.solver.backend.maxsat.MaxSatFormatTranslator#typeToInt} for what is stored.
     */
    protected final Map<Qualifier, Integer> typeToInt;

    public MaxSATAbstractConstraintEncoder(Lattice lattice, Map<Qualifier, Integer> typeToInt) {
        super(lattice, EMPTY_CLAUSE, CONTRADICTORY_CLAUSES);
        this.typeToInt = typeToInt;
    }
}
