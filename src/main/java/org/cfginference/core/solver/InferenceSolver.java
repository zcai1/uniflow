package org.cfginference.core.solver;

import com.sun.tools.javac.util.Context;
import org.cfginference.core.model.constraint.Constraint;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.typesystem.QualifierHierarchy;

import java.util.Collection;
import java.util.Map;

public interface InferenceSolver {

    /**
     * Solve the constraints and return a mapping of slot id to an resulting
     * AnnotationMirror.
     *
     * @param configuration String key value pairs to configure the solver
     * @param slots List of all slots used in inference
     * @param constraints List of Constraints to be satisfied
     * @param qualifierHierarchy Target QualifierHierarchy
     * @return an InferenceResult for the given slots/constraints or NULL if this solver does something besides solve
     */
    InferenceResult solve(Context context,
                          Map<String, String> configuration,
                          Collection<Slot> slots,
                          Collection<Constraint> constraints,
                          QualifierHierarchy qualifierHierarchy);
}
