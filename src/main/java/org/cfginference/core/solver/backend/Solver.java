package org.cfginference.core.solver.backend;

import com.sun.tools.javac.util.Context;
import org.cfginference.core.model.constraint.Constraint;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.solver.frontend.Lattice;
import org.cfginference.core.solver.util.SolverOptions;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Solver adapts a concrete underlying solver, e.g. Sat4j, LogiQL, Z3, etc.
 * This class is the super class for all concrete Solver sub-classes.
 * For each concrete Solver, it adapts the type constraint solving process to
 * the underlying solver implementation.
 *
 * A Solver takes type constraints from {@link checkers.inference.solver.SolverEngine}},
 * then delegates solving constraints responsibility to the underlying solver, and transform
 * underlying solver solution as a map between an integer and an annotation mirror as
 * the inferred result.
 *
 * Method {@link #solve()} is responsible for coordinating
 * above steps.
 *
 * {@link #solve()} method is the entry point of the solver adapter, and it is got
 * called in class {@link checkers.inference.solver.SolverEngine}}. See
 * {@link checkers.inference.solver.SolverEngine#solveInparall()} and
 * {@link checkers.inference.solver.SolverEngine#solveInSequential()}.
 *
 * @author jianchu
 *
 * @param <T> type of FormatTranslator required by this Solver
 */
public abstract class Solver<T extends FormatTranslator<?, ?, ?>> {

    protected final Context context;

    /**
     * SolverOptions, an argument manager for getting options from user.
     */
    protected final SolverOptions solverOptions;

    /**
     * Collection of all slots will be used by underlying solver
     */
    protected final Collection<Slot> slots;

    /**
     * Collection of all constraints will be solved by underlying solver
     */
    protected final Collection<Constraint> constraints;

    /**
     * translator for encoding inference slots and constraints to underlying solver's constraints,
     * and decoding underlying solver's solution back to AnnotationMirrors.
     */
    protected final T formatTranslator;

    /**
     * Set of ids of all variable solts will be used by underlying solver
     */
    protected final Set<Integer> varSlotIds;

    /**
     * Target qualifier lattice
     */
    protected final Lattice lattice;

    public Solver(Context context, SolverOptions solverOptions, Collection<Slot> slots,
                  Collection<Constraint> constraints, T formatTranslator, Lattice lattice) {
        this.context = context;
        this.solverOptions = solverOptions;
        this.slots = slots;
        this.constraints = constraints;
        this.formatTranslator = formatTranslator;
        this.varSlotIds = new LinkedHashSet<>();
        this.lattice = lattice;
    }

    /**
     * A concrete solver adapter needs to override this method and implements its own
     * constraint-solving strategy. In general, there will be three steps in this method:
     * 1. Calls {@link #encodeAllConstraints()}, let {@link FormatTranslator} to convert constraints into
     * the corresponding encoding form. Optionally, encode well-formedness restriction if the backend has it.
     * 2. Calls the underlying solver to solve the encoding.
     * 3. For UNSAT case, returns null. Otherwise let {@link FormatTranslator} decodes the solution from
     * the underlying solver and create a map between an Integer(Slot Id) and an AnnotationMirror as it's
     * inferred annotation.
     *
     * It is the concrete solver adapter's responsibility to implemented the logic of above instructions and
     * statistic collection.
     * See {@link checkers.inference.solver.backend.maxsat.MaxSatSolver#solve()}} for an example.
     */
    public abstract Map<Integer, Qualifier> solve();

    /**
     * Returns a set of constraints that are not solvable together.
     */
    public abstract Collection<Constraint> explainUnsatisfiable();

    /**
     * Calls formatTranslator to convert constraints into the corresponding encoding
     * form. See {@link checkers.inference.solver.backend.maxsat.MaxSatSolver#encodeAllConstraints()}} for an example.
     */
    protected abstract void encodeAllConstraints();

    /**
     * Get slot id from variable slot.
     *
     * @param constraint
     */
    protected void collectVarSlots(Constraint constraint) {
        for (Slot slot : constraint.getSlots()) {
            if (!(slot instanceof ConstantSlot)) {
                this.varSlotIds.add(slot.getId());
            }
        }
    }
}
