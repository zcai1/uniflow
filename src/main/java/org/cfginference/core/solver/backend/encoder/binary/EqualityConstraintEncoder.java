package org.cfginference.core.solver.backend.encoder.binary;

/**
 * A marker interface that all constraint encoders that support encoding {@link checkers.inference.model.EqualityConstraint}
 * should implement. Otherwise, the encoder will be considered not supporting encoding equality constraint and rejected by
 * the {@link AbstractConstraintEncoderFactory#createEqualityConstraintEncoder()}
 *
 * @see checkers.inference.model.EqualityConstraint
 * @see AbstractConstraintEncoderFactory#createEqualityConstraintEncoder()
 */
public interface EqualityConstraintEncoder<ConstraintEncodingT> extends BinaryConstraintEncoder<ConstraintEncodingT> {
}
