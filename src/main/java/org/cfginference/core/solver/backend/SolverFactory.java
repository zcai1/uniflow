package org.cfginference.core.solver.backend;

import org.cfginference.core.model.constraint.Constraint;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.solver.frontend.Lattice;
import org.cfginference.core.solver.util.SolverOptions;

import java.util.Collection;

/**
 * Factory class for creating an underlying {@link Solver}.
 * <p>
 * Note: subclass of this interface should have a zero parameter
 * constructor, and should follow the naming convention to let {@code SolverEngine}
 * reflectively load the subclass instance.
 * <p>
 * Naming convention of solver factory for underlying solvers:
 * <p>
 * Package name should be: checkers.inference.solver.backend.[(all lower cases)underlying solver name]
 * Under this package, create a subclass named: [underlying solver name]SolverFactory.
 * <p>
 * E.g. For MaxSat solver:
 * <p>
 * Package name: checkers.inference.solver.backend.maxsat
 * Class name: MaxSatSolverFactory
 *
 * @see SolverEngine#createSolverFactory()
 */
public interface SolverFactory {

    Solver<?> createSolver(SolverOptions solverOptions,
                           Collection<Slot> slots, Collection<Constraint> constraints, Lattice lattice);
}
