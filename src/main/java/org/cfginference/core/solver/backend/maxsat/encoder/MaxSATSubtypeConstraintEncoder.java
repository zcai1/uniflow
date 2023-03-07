package org.cfginference.core.solver.backend.maxsat.encoder;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.slot.VariableSlot;
import org.cfginference.core.solver.backend.encoder.binary.SubtypeConstraintEncoder;
import org.cfginference.core.solver.backend.maxsat.MathUtils;
import org.cfginference.core.solver.backend.maxsat.VectorUtils;
import org.cfginference.core.solver.frontend.Lattice;
import org.checkerframework.javacutil.AnnotationUtils;
import org.sat4j.core.VecInt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MaxSATSubtypeConstraintEncoder extends MaxSATAbstractConstraintEncoder implements SubtypeConstraintEncoder<VecInt[]> {

    public MaxSATSubtypeConstraintEncoder(Lattice lattice, Map<Qualifier, Integer> typeToInt) {
        super(lattice, typeToInt);
    }

    /**
     * For subtype constraint, if supertype is constant slot, then the subtype
     * cannot be the super type of supertype, same for subtype
     */
    protected VecInt[] getMustNotBe(Set<Qualifier> mustNotBe, Slot vSlot, ConstantSlot cSlot) {

        List<Integer> resultList = new ArrayList<Integer>();

        for (Qualifier sub : mustNotBe) {
            if (!sub.equals(cSlot.getValue())) {
                resultList.add(-MathUtils.mapIdToMatrixEntry(vSlot.getId(), typeToInt.get(sub), lattice));
            }
        }

        VecInt[] result = new VecInt[resultList.size()];
        if (resultList.size() > 0) {
            Iterator<Integer> iterator = resultList.iterator();
            for (int i = 0; i < result.length; i++) {
                result[i] = VectorUtils.asVec(iterator.next().intValue());
            }
            return result;
        }
        return emptyValue;
    }

    protected int[] getMaybe(Qualifier type, Slot knownType, Slot unknownType,
                             Collection<Qualifier> maybeSet) {
        int[] maybeArray = new int[maybeSet.size() + 1];
        int i = 1;
        maybeArray[0] = -MathUtils.mapIdToMatrixEntry(knownType.getId(), typeToInt.get(type), lattice);
        for (Qualifier sup : maybeSet) {
            maybeArray[i] = MathUtils.mapIdToMatrixEntry(unknownType.getId(), typeToInt.get(sup), lattice);
            i++;
        }
        return maybeArray;
    }

    @Override
    public VecInt[] encodeVariable_Variable(VariableSlot subtype, VariableSlot supertype) {
        // if subtype is top, then supertype is top.
        // if supertype is bottom, then subtype is bottom.
        VecInt supertypeOfTop = VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(subtype.getId(), typeToInt.get(lattice.top), lattice),
                MathUtils.mapIdToMatrixEntry(supertype.getId(), typeToInt.get(lattice.top), lattice));
        VecInt subtypeOfBottom = VectorUtils.asVec(
                -MathUtils.mapIdToMatrixEntry(supertype.getId(), typeToInt.get(lattice.bottom), lattice),
                MathUtils.mapIdToMatrixEntry(subtype.getId(), typeToInt.get(lattice.bottom), lattice));

        List<VecInt> resultList = new ArrayList<VecInt>();
        for (Qualifier type : lattice.allTypes) {
            // if we know subtype
            if (!lattice.top.equals(type)) {
                resultList.add(VectorUtils
                        .asVec(getMaybe(type, subtype, supertype, lattice.superType.get(type))));
            }

            // if we know supertype
            if (!lattice.bottom.equals(type)) {
                resultList.add(VectorUtils
                        .asVec(getMaybe(type, supertype, subtype, lattice.subType.get(type))));
            }
        }
        resultList.add(supertypeOfTop);
        resultList.add(subtypeOfBottom);
        VecInt[] result = resultList.toArray(new VecInt[resultList.size()]);
        return result;
    }

    @Override
    public VecInt[] encodeVariable_Constant(VariableSlot subtype, ConstantSlot supertype) {
        final Set<Qualifier> mustNotBe = new HashSet<>();
        if (lattice.bottom.equals(supertype.getValue())) {
            return VectorUtils.asVecArray(
                    MathUtils.mapIdToMatrixEntry(subtype.getId(), typeToInt.get(lattice.bottom), lattice));
        }

        if (lattice.superType.get(supertype.getValue()) != null) {
            mustNotBe.addAll(lattice.superType.get(supertype.getValue()));
        }
        if (lattice.incomparableType.keySet().contains(supertype.getValue())) {
            mustNotBe.addAll(lattice.incomparableType.get(supertype.getValue()));
        }
        return getMustNotBe(mustNotBe, subtype, supertype);
    }

    @Override
    public VecInt[] encodeConstant_Variable(ConstantSlot subtype, VariableSlot supertype) {
        final Set<Qualifier> mustNotBe = new HashSet<>();
        if (lattice.top.equals(subtype.getValue())) {
            return VectorUtils.asVecArray(
                    MathUtils.mapIdToMatrixEntry(supertype.getId(), typeToInt.get(lattice.top), lattice));
        }
        if (lattice.subType.get(subtype.getValue()) != null) {
            mustNotBe.addAll(lattice.subType.get(subtype.getValue()));
        }

        if (lattice.incomparableType.keySet().contains(subtype.getValue())) {
            mustNotBe.addAll(lattice.incomparableType.get(subtype.getValue()));
        }
        return getMustNotBe(mustNotBe, supertype, subtype);
    }
}
