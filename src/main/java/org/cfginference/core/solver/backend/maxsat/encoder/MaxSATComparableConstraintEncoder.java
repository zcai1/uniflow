package org.cfginference.core.solver.backend.maxsat.encoder;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.VariableSlot;
import org.cfginference.core.solver.backend.encoder.binary.ComparableConstraintEncoder;
import org.cfginference.core.solver.backend.maxsat.MathUtils;
import org.cfginference.core.solver.backend.maxsat.VectorUtils;
import org.cfginference.core.solver.frontend.Lattice;
import org.sat4j.core.VecInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MaxSATComparableConstraintEncoder extends MaxSATAbstractConstraintEncoder implements ComparableConstraintEncoder<VecInt[]> {

    public MaxSATComparableConstraintEncoder(Lattice lattice, Map<Qualifier, Integer> typeToInt) {
        super(lattice, typeToInt);
    }

    @Override
    public VecInt[] encodeVariable_Variable(VariableSlot fst, VariableSlot snd) {
        // a <=> !b which is the same as (!a v !b) & (b v a)
        List<VecInt> list = new ArrayList<VecInt>();
        for (Qualifier type : lattice.allTypes) {
            if (lattice.incomparableType.keySet().contains(type)) {
                for (Qualifier notComparable : lattice.incomparableType.get(type)) {
                    list.add(VectorUtils.asVec(
                            -MathUtils.mapIdToMatrixEntry(fst.getId(), typeToInt.get(type), lattice),
                            -MathUtils.mapIdToMatrixEntry(snd.getId(), typeToInt.get(notComparable), lattice),
                            MathUtils.mapIdToMatrixEntry(snd.getId(), typeToInt.get(notComparable), lattice),
                            MathUtils.mapIdToMatrixEntry(fst.getId(), typeToInt.get(type), lattice)));
                }
            }
        }
        VecInt[] result = list.toArray(new VecInt[list.size()]);
        return result;
    }

    @Override
    public VecInt[] encodeVariable_Constant(VariableSlot fst, ConstantSlot snd) {
        if (lattice.incomparableType.keySet().contains(snd.getValue())) {
            List<VecInt> resultList = new ArrayList<>();
            for (Qualifier incomparable : lattice.incomparableType.get(snd.getValue())) {
                // Should not be equal to incomparable
                resultList.add(
                    VectorUtils.asVec(
                        -MathUtils.mapIdToMatrixEntry(fst.getId(), typeToInt.get(incomparable), lattice)));
            }
            VecInt[] resultArray = new VecInt[resultList.size()];
            return resultList.toArray(resultArray);
        } else {
            return emptyValue;
        }
    }

    @Override
    public VecInt[] encodeConstant_Variable(ConstantSlot fst, VariableSlot snd) {
        return encodeVariable_Constant(snd, fst);
    }
}
