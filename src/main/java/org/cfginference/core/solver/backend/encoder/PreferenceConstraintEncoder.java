package org.cfginference.core.solver.backend.encoder;

import org.cfginference.core.model.constraint.PreferenceConstraint;

/**
 * Interface that defines operations to encode a {@link checkers.inference.model.PreferenceConstraint}.
 */
public interface PreferenceConstraintEncoder<ConstraintEncodingT> {

    ConstraintEncodingT encode(PreferenceConstraint constraint);
}
