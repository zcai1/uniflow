package org.cfginference.core.solver.backend.maxsat.encoder;

import org.cfginference.core.model.constraint.PreferenceConstraint;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.VariableSlot;
import org.cfginference.core.solver.backend.encoder.PreferenceConstraintEncoder;
import org.cfginference.core.solver.backend.maxsat.MathUtils;
import org.cfginference.core.solver.backend.maxsat.VectorUtils;
import org.cfginference.core.solver.frontend.Lattice;
import org.sat4j.core.VecInt;

import java.util.Map;

public class MaxSATPreferenceConstraintEncoder extends MaxSATAbstractConstraintEncoder implements PreferenceConstraintEncoder<VecInt[]> {

    public MaxSATPreferenceConstraintEncoder(Lattice lattice, Map<Qualifier, Integer> typeToInt) {
        super(lattice, typeToInt);
    }

    // TODO: we should consider the situation that the type annotations with
    // different weights.
    @Override
    public VecInt[] encode(PreferenceConstraint constraint) {
        VariableSlot vs = constraint.getVariable();
        ConstantSlot cs = constraint.getGoal();
        if (lattice.allTypes.contains(cs.getValue())) {
            return VectorUtils.asVecArray(MathUtils.mapIdToMatrixEntry(vs.getId(), typeToInt.get(cs.getValue()),
                    lattice));
        } else {
            return emptyValue;
        }
    }
}
