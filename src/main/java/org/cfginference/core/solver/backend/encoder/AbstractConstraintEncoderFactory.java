package org.cfginference.core.solver.backend.encoder;

import org.cfginference.core.solver.backend.FormatTranslator;
import org.cfginference.core.solver.frontend.Lattice;

public abstract class AbstractConstraintEncoderFactory<ConstraintEncodingT, FormatTranslatorT
        extends FormatTranslator<?, ConstraintEncodingT, ?>>
        implements ConstraintEncoderFactory<ConstraintEncodingT> {

    /**
     * {@link Lattice} instance that every constraint encoder needs
     */
    protected final Lattice lattice;

    /**
     * {@link FormatTranslator} instance that concrete subclass of {@link AbstractConstraintEncoder} might need.
     * For example, {@link checkers.inference.solver.backend.z3.encoder.Z3BitVectorSubtypeConstraintEncoder} needs
     * it to format translate {@SubtypeConstraint}. {@link checkers.inference.solver.backend.maxsat.encoder.MaxSATImplicationConstraintEncoder}
     * needs it to delegate format translation task of non-{@code ImplicationConstraint}s.
     */
    protected final FormatTranslatorT formatTranslator;

    public AbstractConstraintEncoderFactory(Lattice lattice, FormatTranslatorT formatTranslator) {
        this.lattice = lattice;
        this.formatTranslator = formatTranslator;
    }
}
