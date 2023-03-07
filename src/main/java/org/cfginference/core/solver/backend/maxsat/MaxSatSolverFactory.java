package org.cfginference.core.solver.backend.maxsat;

import com.sun.tools.javac.util.Context;
import org.cfginference.core.model.constraint.Constraint;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.solver.backend.AbstractSolverFactory;
import org.cfginference.core.solver.backend.Solver;
import org.cfginference.core.solver.frontend.Lattice;
import org.cfginference.core.solver.util.SolverOptions;

import java.util.Collection;

public class MaxSatSolverFactory extends AbstractSolverFactory<MaxSatFormatTranslator> {

    private final Context context;

    public MaxSatSolverFactory(Context context) {
        this.context = context;

        context.put(MaxSatSolverFactory.class, this);
    }

    public static MaxSatSolverFactory instance(Context context) {
        MaxSatSolverFactory instance = context.get(MaxSatSolverFactory.class);
        if (instance == null) {
            instance = new MaxSatSolverFactory(context);
        }
        return instance;
    }

    @Override
    public Solver<?> createSolver(SolverOptions options, Collection<Slot> slots,
                                  Collection<Constraint> constraints, Lattice lattice) {
        MaxSatFormatTranslator formatTranslator = createFormatTranslator(lattice);
        return new MaxSatSolver(context, options, slots, constraints, formatTranslator, lattice);
    }

    @Override
    public MaxSatFormatTranslator createFormatTranslator(Lattice lattice) {
        return new MaxSatFormatTranslator(lattice);
    }
}
