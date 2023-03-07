package org.cfginference.core.solver.backend.maxsat;

import com.sun.tools.javac.util.Context;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.solver.backend.AbstractFormatTranslator;
import org.cfginference.core.solver.backend.encoder.ConstraintEncoderFactory;
import org.cfginference.core.solver.backend.maxsat.encoder.MaxSATConstraintEncoderFactory;
import org.cfginference.core.solver.frontend.Lattice;
import org.sat4j.core.VecInt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MaxSatFormatTranslator converts constraint into array of VecInt as clauses.
 *
 * @author jianchu
 *
 */

public class MaxSatFormatTranslator extends AbstractFormatTranslator<VecInt[], VecInt[], Integer> {

    /**
     * typeToInt maps each type qualifier to an unique integer value starts from
     * 0 on continuous basis.
     */
    protected final Map<Qualifier, Integer> typeToInt;

    /**
     * intToType maps an integer value to each type qualifier, which is a
     * reversed map of typeToInt.
     */
    protected final Map<Integer, Qualifier> intToType;

    public MaxSatFormatTranslator(Lattice lattice) {
        super(lattice);
        // Initialize mappings between type and int.
        Map<Qualifier, Integer> typeToIntRes = new LinkedHashMap<>();
        Map<Integer, Qualifier> intToTypeRes = new LinkedHashMap<>();

        int curInt = 0;
        for (Qualifier type : lattice.allTypes) {
            typeToIntRes.put(type, curInt);
            intToTypeRes.put(curInt, type);
            curInt ++;
        }

        typeToInt = Collections.unmodifiableMap(typeToIntRes);
        intToType = Collections.unmodifiableMap(intToTypeRes);
        finishInitializingEncoders();
    }

    @Override
    protected ConstraintEncoderFactory<VecInt[]> createConstraintEncoderFactory() {
        return new MaxSATConstraintEncoderFactory(lattice, typeToInt, this);
    }

    /**
     * generate well form clauses such that there is one and only one beta value
     * can be true.
     *
     */
    public void generateWellFormednessClauses(List<VecInt> wellFormednessClauses, Integer varSlotId) {
        int[] leastOneIsTrue = new int[lattice.numTypes];
        for (Integer i : intToType.keySet()) {
            leastOneIsTrue[i] = MathUtils.mapIdToMatrixEntry(varSlotId, i.intValue(), lattice);
        }
        wellFormednessClauses.add(VectorUtils.asVec(leastOneIsTrue));
        List<Integer> varList = new ArrayList<Integer>(intToType.keySet());
        for (int i = 0; i < varList.size(); i++) {
            for (int j = i + 1; j < varList.size(); j++) {
                VecInt vecInt = new VecInt(2);
                vecInt.push(-MathUtils.mapIdToMatrixEntry(varSlotId, varList.get(i), lattice));
                vecInt.push(-MathUtils.mapIdToMatrixEntry(varSlotId, varList.get(j), lattice));
                wellFormednessClauses.add(vecInt);
            }
        }
    }

    @Override
    public Qualifier decodeSolution(Integer solution, Context context) {
        return intToType.get(MathUtils.getIntRep(solution, lattice));
    }
}
