package org.cfginference.core.solver.backend.encoder;

import org.cfginference.core.model.constraint.ImplicationConstraint;

/**
 * Interface that defines operations to encode a {@link ImplicationConstraint}.
 *
 * @param <ConstraintEncodingT> solver encoding type for {@link ImplicationConstraint}
 */
public interface ImplicationConstraintEncoder<ConstraintEncodingT> {

    ConstraintEncodingT encode(ImplicationConstraint constraint);
}
