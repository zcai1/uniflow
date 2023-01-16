package org.cfginference.core.solver.backend.encoder;

import org.cfginference.core.solver.backend.encoder.binary.ComparableConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.binary.EqualityConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.binary.InequalityConstraintEncoder;
import org.cfginference.core.solver.backend.encoder.binary.SubtypeConstraintEncoder;

/**
 * Factory that creates constraint encoders.
 *
 * <p>
 * Right now, {@link ConstraintEncoderFactory} interface supports creation of these encoders:
 * <ul>
 *     <li>{@link SubtypeConstraintEncoder}</li>
 *     <li>{@link EqualityConstraintEncoder}</li>
 *     <li>{@link InequalityConstraintEncoder}</li>
 *     <li>{@link ComparableConstraintEncoder}</li>
 *     <li>{@link ComparisonConstraintEncoder}</li>
 *     <li>{@link PreferenceConstraintEncoder}</li>
 *     <li>{@link ViewpointAdaptationConstraintEncoder}</li>
 *     <li>{@link ExistentialConstraintEncoder}</li>
 *     <li>{@link ImplicationConstraintEncoder}</li>
 *     <li>{@link ArithmeticConstraintEncoder}</li>
 * </ul>
 * <p>
 * User of this interface is {@link org.cfginference.core.solver.backend.AbstractFormatTranslator}
 * and its subclasses.
 *
 * @see org.cfginference.core.solver.backend.AbstractFormatTranslator
 */
public interface ConstraintEncoderFactory<ConstraintEncodingT> {

    SubtypeConstraintEncoder<ConstraintEncodingT> createSubtypeConstraintEncoder();

    EqualityConstraintEncoder<ConstraintEncodingT> createEqualityConstraintEncoder();

    InequalityConstraintEncoder<ConstraintEncodingT> createInequalityConstraintEncoder();

    ComparableConstraintEncoder<ConstraintEncodingT> createComparableConstraintEncoder();

    ComparisonConstraintEncoder<ConstraintEncodingT> createComparisonConstraintEncoder();

    PreferenceConstraintEncoder<ConstraintEncodingT> createPreferenceConstraintEncoder();

    ViewpointAdaptationConstraintEncoder<ConstraintEncodingT> createViewpointAdaptationConstraintEncoder();

    ExistentialConstraintEncoder<ConstraintEncodingT> createExistentialConstraintEncoder();

    ImplicationConstraintEncoder<ConstraintEncodingT> createImplicationConstraintEncoder();

    ArithmeticConstraintEncoder<ConstraintEncodingT> createArithmeticConstraintEncoder();
}
