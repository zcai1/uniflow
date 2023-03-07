package org.cfginference.core.solver;

import com.sun.tools.javac.util.Context;
import org.cfginference.core.model.constraint.AlwaysFalseConstraint;
import org.cfginference.core.model.constraint.Constraint;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.slot.SlotManager;
import org.cfginference.core.solver.serializer.CnfVecIntSerializer;
import org.cfginference.core.typesystem.QualifierHierarchy;
import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;
import org.sat4j.pb.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This solver is used to convert any constraint set using a type system with only 2 types (Top/Bottom),
 * into a SAT problem.  This SAT problem is then solved by SAT4J and the output is converted back
 * into an InferenceResult.
 */
public class MaxSat2TypeSolver implements InferenceSolver {

    // private QualifierHierarchy qualHierarchy;
    private Collection<Constraint> constraints;
    // private Collection<Slot> slots;

    // private Qualifier defaultValue;
    private Qualifier top;
    private Qualifier bottom;
    private CnfVecIntSerializer serializer;
    private SlotManager slotManager;

    @Override
    public InferenceResult solve(Context context,
                                 Map<String, String> configuration,
                                 Collection<Slot> slots,
                                 Collection<Constraint> constraints,
                                 QualifierHierarchy qualifierHierarchy) {
        // TODO: how to properly handle "always false" in constraints?
        AlwaysFalseConstraint alwaysFalseConstraint = AlwaysFalseConstraint.instance();
        if (constraints.contains(alwaysFalseConstraint)) {
            return new DefaultInferenceResult(Set.of(alwaysFalseConstraint));
        }

        // this.slots = slots;
        this.constraints = constraints;
        // this.qualHierarchy = qualHierarchy;

        this.top = qualifierHierarchy.getTopQualifier();
        this.bottom = qualifierHierarchy.getBottomQualifier();
        this.slotManager = SlotManager.instance(context);
        this.serializer = new CnfVecIntSerializer(context) {
            @Override
            protected boolean isTop(ConstantSlot constantSlot) {
                return constantSlot.getValue().equals(top);
            }
        };
        // TODO: This needs to be parameterized based on the type system
        // this.defaultValue = top;

        return solve();
    }

    private InferenceResult solve() {

        final List<VecInt> softClauses = new LinkedList<>();
        final List<VecInt> hardClauses = new LinkedList<>();
        serializer.convertAll(constraints, hardClauses, softClauses);

        // nextId describes the LARGEST id that might be found in a variable
        // if an exception occurs while creating a variable the id might be incremented
        // but the slot might not actually be recorded.  Therefore, nextId is NOT
        // the number of slots but the maximum you might encounter.
        // TODO: this is a workaround as currently when serialize existential constraint we lost the real existential
        // TODO: variable id and create "fake" id stored in existentialToPotentialVar map.
        // TODO: thus here the value of totalVars is the real slots number stored in slotManager, and plus the
        // TODO: "fake" slots number stored in existentialToPotentialVar
        final int totalVars = slotManager.getSlots().size() + serializer.getExistentialToPotentialVar().size();
        final int totalClauses = hardClauses.size() + softClauses.size();


        // When .newBoth is called, SAT4J will run two solvers and return the result of the first to halt
        final WeightedMaxSatDecorator solver = new WeightedMaxSatDecorator(SolverFactory.newBoth());

        solver.newVar(totalVars);
        solver.setExpectedNumberOfClauses(totalClauses);

        // arbitrary timeout selected for no particular reason
        solver.setTimeoutMs(1000000);

        VecInt lastClause = null;
        try {
            for (VecInt clause : hardClauses) {
                lastClause = clause;
                solver.addHardClause(clause);
            }
            for (VecInt clause : softClauses) {

                lastClause = clause;
                solver.addSoftClause(clause);
            }

        } catch (ContradictionException ce) {
            // This happens when adding a clause causes trivial contradiction, such as adding -1 to {1}
            System.out.println("Not solvable! Contradiction exception " +
                    "when adding clause: " + lastClause + ".");

            // pass empty set as the unsat explanation
            // TODO: explain UNSAT possibly by reusing MaxSatSolver.MaxSATUnsatisfiableConstraintExplainer
            return new DefaultInferenceResult(new HashSet<>());
        }

        boolean isSatisfiable;
        try {
            // isSatisfiable() launches the solvers and waits until one of them finishes
            isSatisfiable = solver.isSatisfiable();

        } catch(TimeoutException te) {
            throw new RuntimeException("MAX-SAT solving timeout! ");
        }

        if (!isSatisfiable) {
            System.out.println("Not solvable!");
            // pass empty set as the unsat explanation
            // TODO: explain UNSAT possibly by reusing MaxSatSolver.MaxSATUnsatisfiableConstraintExplainer
            return new DefaultInferenceResult(new HashSet<>());
        }

        int[] solution = solver.model();
        // The following code decodes VecInt solution to the slot-annotation mappings
        final Map<Integer, Qualifier> decodedSolution = new HashMap<>();
        final Map<Integer, Integer> existentialToPotentialIds = serializer.getExistentialToPotentialVar();

        for (Integer var : solution) {
            Integer potential = existentialToPotentialIds.get(Math.abs(var));
            if (potential != null) {
                // Assume the 'solution' output by the solver is already sorted in the ascending order
                // of their absolute values. So the existential variables come after the potential variables,
                // which means the potential slot corresponding to the current existential variable is
                // already inserted into 'solutions'
                assert decodedSolution.containsKey(potential);
                if (var < 0) {
                    // The existential variable is false, so the potential variable should not be inserted.
                    // Remove it from the solution.
                    decodedSolution.remove(potential);
                }
            } else {
                boolean isTop = var < 0;
                if (isTop) {
                    var = -var;
                }
                decodedSolution.put(var, isTop ? top : bottom);
            }
        }

        return new DefaultInferenceResult(decodedSolution);
    }
}
