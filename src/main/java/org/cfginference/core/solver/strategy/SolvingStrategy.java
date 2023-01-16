package org.cfginference.core.solver.strategy;

import org.cfginference.core.model.constraint.Constraint;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.solver.InferenceResult;
import org.cfginference.core.solver.frontend.Lattice;

import java.util.Collection;

/**
 * Define a strategy on solving constriants.
 * <p>
 * Note: subclasses within the Solver Framework should follow naming conventions,
 * in order to let {@code SolverEngine} be able to reflectively load subclass instance.
 * <p>
 * Naming convention is:
 * Package: subclasses should be created within current package checkers.inference.solver.strategy.
 * Class name: [StrategyName]SolvingStrategy.
 * <p>
 * E.g. For graph solving strategy, the class name should be: GraphSolvingStrategy.
 *
 * @see SolverEngine#createSolvingStrategy()
 */
public interface SolvingStrategy {

    /**
     * Solve the constraints by the solving strategy defined in this method.
     *
     */
    InferenceResult solve(Collection<Slot> slots, Collection<Constraint> constraints, Lattice lattice);
}
