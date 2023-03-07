package org.cfginference.core.solver.backend.maxsat.encoder;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.solver.backend.encoder.AbstractConstraintEncoderFactory;
import org.cfginference.core.solver.backend.encoder.ArithmeticConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.ComparisonConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.ExistentialConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.ViewpointAdaptationConstraintEncoder;
import org.cfginference.core.solver.backend.maxsat.MaxSatFormatTranslator;
import org.cfginference.core.solver.frontend.Lattice;
import org.sat4j.core.VecInt;

import java.util.Map;

/**
 * MaxSAT implementation of {@link checkers.inference.solver.backend.encoder.ConstraintEncoderFactory}.
 *
 * @see checkers.inference.solver.backend.encoder.ConstraintEncoderFactory
 */
public class MaxSATConstraintEncoderFactory extends AbstractConstraintEncoderFactory<VecInt[], MaxSatFormatTranslator> {

    private final Map<Qualifier, Integer> typeToInt;

    public MaxSATConstraintEncoderFactory(Lattice lattice, Map<Qualifier, Integer> typeToInt,
                                          MaxSatFormatTranslator formatTranslator) {
        super(lattice, formatTranslator);
        this.typeToInt = typeToInt;
    }

    @Override
    public MaxSATSubtypeConstraintEncoder createSubtypeConstraintEncoder() {
        return new MaxSATSubtypeConstraintEncoder(lattice, typeToInt);
    }

    @Override
    public MaxSATEqualityConstraintEncoder createEqualityConstraintEncoder() {
        return new MaxSATEqualityConstraintEncoder(lattice, typeToInt);
    }

    @Override
    public MaxSATInequalityConstraintEncoder createInequalityConstraintEncoder() {
        return new MaxSATInequalityConstraintEncoder(lattice, typeToInt);
    }

    @Override
    public MaxSATComparableConstraintEncoder createComparableConstraintEncoder() {
        return new MaxSATComparableConstraintEncoder(lattice, typeToInt);
    }

    @Override
    public ComparisonConstraintEncoder<VecInt[]> createComparisonConstraintEncoder() {
        return null;
    }

    @Override
    public MaxSATPreferenceConstraintEncoder createPreferenceConstraintEncoder() {
        return new MaxSATPreferenceConstraintEncoder(lattice, typeToInt);
    }

    @Override
    public MaxSATImplicationConstraintEncoder createImplicationConstraintEncoder() {
        return new MaxSATImplicationConstraintEncoder(lattice, typeToInt, formatTranslator);
    }

    @Override
    public ViewpointAdaptationConstraintEncoder<VecInt[]> createViewpointAdaptationConstraintEncoder() {
        return null;
    }

    @Override
    public ExistentialConstraintEncoder<VecInt[]> createExistentialConstraintEncoder() {
        return null;
    }

    @Override
    public ArithmeticConstraintEncoder<VecInt[]> createArithmeticConstraintEncoder() {
        return null;
    }
}
