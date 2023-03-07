package org.uniflow.core.solver;

import org.uniflow.core.model.constraint.Constraint;
import org.uniflow.core.model.qualifier.Qualifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default implementation of {@link InferenceResult}.
 *
 * This class'es implementation follows this pattern:
 * No solution == {@link #varIdToQualifier} is null
 * Has solution == empty {@link #varIdToQualifier solution} or non-empty {@link #varIdToQualifier solution}
 *
 * @see {@link #hasSolution()}
 */
public class DefaultInferenceResult implements InferenceResult {

    /**
     * A map from variable Id of {@link checkers.inference.model.Slot Slot}
     * to {@link Qualifier}.
     *
     * No solution should set this field to null. Otherwise, if the map is empty, it means
     * empty solution(solution with no variable IDs). This happens for trivial testcase that
     * doesn't need to insert annotations to source code.
     *
     * @see #hasSolution()
     */
    protected final Map<Integer, Qualifier> varIdToQualifier;

    /**
     * Set of {@link Constraint}s that caused solver not being able to give solutions.
     *
     * If {@link #varIdToQualifier} is null, there is no solution. This field then needs to
     * passed in a non-null non-empty set as explanation. But if a solver backend doesn't support
     * explanation feature, an empty set is allowed to be passed in, so that caller knows the backend
     * can not explain unsolvable reason.
     * If {@link #varIdToQualifier} is non-null, solution exists. In this case, this field
     * is not accessed(otherwise, throw {@code UnsupportedOperationException}), so it does't really
     * matter what to be passed in here. But conservatively, an empty set is preferred. There is a
     * dedicated constructor {@link DefaultInferenceResult(Map<Integer, Qualifier>)} for this
     * situation. For the best pratice, client should always call this constructor for cases with solutions.
     */
    protected final Collection<Constraint> unsatisfiableConstraints;

    /**
     * No-arg constructor.
     *
     * Should be called in situations:
     * 1) Try to create empty solution
     * 2) Subclass calls this super constructor to begin with an empty map. Then subclass
     * has its logic to adding solutions to the mapping {@link #varIdToQualifier}. The existing two
     * subclasses are: {@link dataflow.solvers.classic.DataflowResult} and {@link sparta.checkers.sat.IFlowResult}.
     */
    public DefaultInferenceResult() {
        this(new LinkedHashMap<>());
    }

    /**
     * One-arg constructor that accepts {@code varIdToAnnotation}.
     *
     * Should be called when inference has solutions(either empty or non-empty, but never
     * null). This case should be the most frequent.
     *
     * @param varIdToQualifier mapping from variable ID to inferred solution {@code Qualifier}
     */
    public DefaultInferenceResult(Map<Integer, Qualifier> varIdToQualifier) {
        this(varIdToQualifier, new HashSet<>());
    }

    /**
     * One-arg constructor that accepts {@code unsatisfiableConstraints}.
     *
     * Should be called when inference failed to give solutions.
     *
     * @param unsatisfiableConstraints non-null set of unsolable constraints. If a solver backend doesn't
     *                                 support explaining, empty set should be passed.
     */
    public DefaultInferenceResult(Collection<Constraint> unsatisfiableConstraints) {
        this(null, unsatisfiableConstraints);
    }

    private DefaultInferenceResult(Map<Integer, Qualifier> varIdToQualifier,
                                   Collection<Constraint> unsatisfiableConstraints) {
        if (unsatisfiableConstraints == null) {
            throw new IllegalArgumentException("unsatisfiableConstraints should never be null!");
        }
        this.varIdToQualifier = varIdToQualifier;
        this.unsatisfiableConstraints = unsatisfiableConstraints;
    }

    @Override
    public boolean hasSolution() {
        return varIdToQualifier != null;
    }

    @Override
    public Map<Integer, Qualifier> getSolutions() {
        if (!hasSolution()) {
            return null;
        }
        return varIdToQualifier;
    }

    @Override
    public boolean containsSolutionForVariable(int varId) {
        if (!hasSolution()) {
            return false;
        }
        return varIdToQualifier.containsKey(varId);
    }

    @Override
    public Qualifier getSolutionForVariable(int varId) {
        if (!hasSolution()) {
            return null;
        }
        return varIdToQualifier.get(varId);
    }

    @Override
    public Collection<Constraint> getUnsatisfiableConstraints() {
        if (hasSolution()) {
            throw new UnsupportedOperationException(
                    "There is solution, calling `getUnsatisfiableConstraints()` is forbidden!");
        }
        return unsatisfiableConstraints;
    }
}
