package org.cfginference.core.solver.backend;

import org.cfginference.core.model.constraint.ArithmeticConstraint;
import org.cfginference.core.model.constraint.ComparableConstraint;
import org.cfginference.core.model.constraint.ComparisonConstraint;
import org.cfginference.core.model.constraint.EqualityConstraint;
import org.cfginference.core.model.constraint.ExistentialConstraint;
import org.cfginference.core.model.constraint.ImplicationConstraint;
import org.cfginference.core.model.constraint.InequalityConstraint;
import org.cfginference.core.model.constraint.PreferenceConstraint;
import org.cfginference.core.model.constraint.SubtypeConstraint;
import org.cfginference.core.model.constraint.ViewpointAdaptationConstraint;
import org.cfginference.core.model.slot.ArithmeticSlot;
import org.cfginference.core.model.slot.ComparisonSlot;
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.ExistentialSlot;
import org.cfginference.core.model.slot.MergeSlot;
import org.cfginference.core.model.slot.PolymorphicInstanceSlot;
import org.cfginference.core.model.slot.RefinementSlot;
import org.cfginference.core.model.slot.SourceSlot;
import org.cfginference.core.model.slot.ViewpointAdaptationSlot;
import org.cfginference.core.solver.backend.encoder.ArithmeticConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.ComparisonConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.ConstraintEncoderCoordinator;
import org.cfginference.core.solver.backend.encoder.ConstraintEncoderFactory;
import org.cfginference.core.solver.backend.encoder.ExistentialConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.ImplicationConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.PreferenceConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.ViewpointAdaptationConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.binary.ComparableConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.binary.EqualityConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.binary.InequalityConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.binary.SubtypeConstraintEncoder;
import org.cfginference.core.solver.frontend.Lattice;

public abstract class AbstractFormatTranslator<SlotEncodingT, ConstraintEncodingT, SlotSolutionT>
        implements FormatTranslator<SlotEncodingT, ConstraintEncodingT, SlotSolutionT> {

    /**
     * {@link Lattice} that is used by subclasses during format translation.
     */
    protected final Lattice lattice;

    /**
     * {@code SubtypeConstraintEncoder} to which encoding of {@link SubtypeConstraint} is delegated.
     */
    protected SubtypeConstraintEncoder<ConstraintEncodingT> subtypeConstraintEncoder;

    /**
     * {@code EqualityConstraintEncoder} to which encoding of {@link EqualityConstraint} is delegated.
     */
    protected EqualityConstraintEncoder<ConstraintEncodingT> equalityConstraintEncoder;

    /**
     * {@code InequalityConstraintEncoder} to which encoding of {@link InequalityConstraint} is delegated.
     */
    protected InequalityConstraintEncoder<ConstraintEncodingT> inequalityConstraintEncoder;

    /**
     * {@code ComparableConstraintEncoder} to which encoding of {@link ComparableConstraint} is delegated.
     */
    protected ComparableConstraintEncoder<ConstraintEncodingT> comparableConstraintEncoder;

    /**
     * {@code ComparisonConstraintEncoder} to which encoding of {@link ComparableConstraint} is delegated.
     */
    protected ComparisonConstraintEncoder<ConstraintEncodingT> comparisonConstraintEncoder;

    /**
     * {@code PreferenceConstraintEncoder} to which encoding of {@link PreferenceConstraint} is delegated.
     */
    protected PreferenceConstraintEncoder<ConstraintEncodingT> preferenceConstraintEncoder;

    /**
     * {@code CombineConstraintEncoder} to which encoding of {@link ViewpointAdaptationConstraint} is delegated.
     */
    protected ViewpointAdaptationConstraintEncoder<ConstraintEncodingT> viewpointAdaptationConstraintEncoder;

    /**
     * {@code ExistentialConstraintEncoder} to which encoding of {@link ExistentialConstraint} is delegated.
     */
    protected ExistentialConstraintEncoder<ConstraintEncodingT> existentialConstraintEncoder;

    protected ImplicationConstraintEncoder<ConstraintEncodingT> implicationConstraintEncoder;

    /**
     * {@code ArithmeticConstraintEncoder} to which encoding of {@link ArithmeticConstraint} is delegated.
     */
    protected ArithmeticConstraintEncoder<ConstraintEncodingT> arithmeticConstraintEncoder;

    public AbstractFormatTranslator(Lattice lattice) {
        this.lattice = lattice;
    }

    /**
     * Creates concrete implementation of {@link ConstraintEncoderFactory}. Subclasses should implement this method
     * to provide their concrete {@code ConstraintEncoderFactory}.
     *
     * @return Concrete implementation of {@link ConstraintEncoderFactory} for a particular solver backend
     */
    protected abstract ConstraintEncoderFactory<ConstraintEncodingT> createConstraintEncoderFactory();

    /**
     * Finishes initializing encoders for subclasses of {@code AbstractFormatTranslator}. Subclasses of
     * {@code AbstractFormatTranslator} MUST call this method to finish initializing encoders at the end
     * of initialization phase. See Javadoc on {@link AbstractFormatTranslator} to see what the last
     * step of initialization phase means and why the encoder creation steps are separate out from constructor
     * {@link AbstractFormatTranslator#AbstractFormatTranslator(Lattice)}
     */
    protected void finishInitializingEncoders() {
        final ConstraintEncoderFactory<ConstraintEncodingT> encoderFactory = createConstraintEncoderFactory();
        subtypeConstraintEncoder = encoderFactory.createSubtypeConstraintEncoder();
        equalityConstraintEncoder = encoderFactory.createEqualityConstraintEncoder();
        inequalityConstraintEncoder = encoderFactory.createInequalityConstraintEncoder();
        comparableConstraintEncoder = encoderFactory.createComparableConstraintEncoder();
        comparisonConstraintEncoder = encoderFactory.createComparisonConstraintEncoder();
        preferenceConstraintEncoder = encoderFactory.createPreferenceConstraintEncoder();
        viewpointAdaptationConstraintEncoder = encoderFactory.createViewpointAdaptationConstraintEncoder();
        existentialConstraintEncoder = encoderFactory.createExistentialConstraintEncoder();
        implicationConstraintEncoder = encoderFactory.createImplicationConstraintEncoder();
        arithmeticConstraintEncoder = encoderFactory.createArithmeticConstraintEncoder();
    }

    @Override
    public final SlotEncodingT serialize(SourceSlot slot) {
        return null;
    }

    @Override
    public final SlotEncodingT serialize(ConstantSlot slot) {
        return null;
    }

    @Override
    public final SlotEncodingT serialize(ExistentialSlot slot) {
        return null;
    }

    @Override
    public final SlotEncodingT serialize(RefinementSlot slot) {
        return null;
    }

    @Override
    public final SlotEncodingT serialize(ViewpointAdaptationSlot slot) {
        return null;
    }

    @Override
    public final SlotEncodingT serialize(MergeSlot slot) {
        return null;
    }

    @Override
    public final SlotEncodingT serialize(ComparisonSlot slot) {
        return null;
    }

    @Override
    public final SlotEncodingT serialize(ArithmeticSlot slot) {
        return null;
    }

    @Override
    public final SlotEncodingT serialize(PolymorphicInstanceSlot slot) {
        // TODO
        return null;
    }

    @Override
    public ConstraintEncodingT serialize(SubtypeConstraint constraint) {
        return subtypeConstraintEncoder == null ? null :
                ConstraintEncoderCoordinator.dispatch(constraint, subtypeConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(EqualityConstraint constraint) {
        return equalityConstraintEncoder == null ? null :
                ConstraintEncoderCoordinator.dispatch(constraint, equalityConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(ExistentialConstraint constraint) {
        return existentialConstraintEncoder == null ? null :
                ConstraintEncoderCoordinator.redirect(constraint, existentialConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(InequalityConstraint constraint) {
        return inequalityConstraintEncoder == null ? null :
                ConstraintEncoderCoordinator.dispatch(constraint, inequalityConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(ComparableConstraint constraint) {
        return comparableConstraintEncoder == null ? null :
                ConstraintEncoderCoordinator.dispatch(constraint, comparableConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(ComparisonConstraint constraint) {
        return comparisonConstraintEncoder == null ? null :
                ConstraintEncoderCoordinator.dispatch(constraint, comparisonConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(ViewpointAdaptationConstraint constraint) {
        return viewpointAdaptationConstraintEncoder == null ? null :
                ConstraintEncoderCoordinator.dispatch(constraint, viewpointAdaptationConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(PreferenceConstraint constraint) {
        return preferenceConstraintEncoder == null ? null :
                ConstraintEncoderCoordinator.redirect(constraint, preferenceConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(ImplicationConstraint constraint) {
        return implicationConstraintEncoder == null ? null :
                ConstraintEncoderCoordinator.redirect(constraint, implicationConstraintEncoder);
    }

    @Override
    public ConstraintEncodingT serialize(ArithmeticConstraint constraint) {
        return arithmeticConstraintEncoder == null ? null :
                ConstraintEncoderCoordinator.dispatch(constraint, arithmeticConstraintEncoder);
    }
}
