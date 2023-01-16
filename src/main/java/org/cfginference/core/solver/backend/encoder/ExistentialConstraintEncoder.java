package org.cfginference.core.solver.backend.encoder;

import org.cfginference.core.model.constraint.ExistentialConstraint;

/**
 * Interface that defines operations to encode a {@link checkers.inference.model.ExistentialConstraint}.
 */
public interface ExistentialConstraintEncoder<ConstraintEncodingT> {

    ConstraintEncodingT encode(ExistentialConstraint constraint);
}
