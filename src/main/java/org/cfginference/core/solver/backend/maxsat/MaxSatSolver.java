package org.cfginference.core.solver.backend.maxsat;

import com.sun.tools.javac.util.Context;
import org.cfginference.core.model.constraint.Constraint;
import org.cfginference.core.model.constraint.PreferenceConstraint;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.slot.SlotManager;
import org.cfginference.core.solver.backend.Solver;
import org.cfginference.core.solver.frontend.Lattice;
import org.cfginference.core.solver.util.FileUtils;
import org.cfginference.core.solver.util.SolverArg;
import org.cfginference.core.solver.util.SolverOptions;
import org.cfginference.core.solver.util.Statistics;
import org.checkerframework.javacutil.BugInCF;
import org.plumelib.util.Pair;
import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.maxsat.WeightedMaxSatDecorator;
import org.sat4j.pb.IPBSolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.tools.xplain.DeletionStrategy;
import org.sat4j.tools.xplain.Xplain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MaxSatSolver calls MaxSatFormatTranslator that converts constraint into a list of
 * VecInt, then invoke Sat4j lib to solve the clauses, and decode the result.
 *
 * @author jianchu
 *
 */
public class MaxSatSolver extends Solver<MaxSatFormatTranslator> {

    private static final Logger logger = LoggerFactory.getLogger(MaxSatSolver.class);

    protected enum MaxSatSolverArg implements SolverArg {
        /**
         * Whether should print the CNF formulas.
         */
        outputCNF;
    }

    protected final SlotManager slotManager;
    protected final List<VecInt> hardClauses = new LinkedList<>();
    private List<VecInt> wellFormednessClauses = new LinkedList<>();
    protected final List<Pair<VecInt, Integer>> softClauses = new LinkedList<>();
    private MaxSATUnsatisfiableConstraintExplainer unsatisfiableConstraintExplainer;
    protected final File CNFData = new File(new File("").getAbsolutePath() + "/cnfData");
    protected StringBuilder CNFInput = new StringBuilder();

    private long serializationStart;
    private long serializationEnd;
    protected long solvingStart;
    protected long solvingEnd;

    public MaxSatSolver(Context context, SolverOptions solverOptions, Collection<Slot> slots,
                        Collection<Constraint> constraints, MaxSatFormatTranslator formatTranslator, Lattice lattice) {
        super(context, solverOptions, slots, constraints, formatTranslator, lattice);
        this.slotManager = SlotManager.instance(context);

        if (shouldOutputCNF()) {
            CNFData.mkdir();
        }
    }

    @Override
    public Map<Integer, Qualifier> solve() {

        Map<Integer, Qualifier> solutions = null;
        final WeightedMaxSatDecorator solver = new WeightedMaxSatDecorator(
                org.sat4j.pb.SolverFactory.newBoth());

        this.serializationStart = System.currentTimeMillis();
        // Serialization step:
        encodeAllConstraints();
        encodeWellFormednessRestriction();
        this.serializationEnd = System.currentTimeMillis();

        if (shouldOutputCNF()) {
            buildCNFInput();
            writeCNFInput();
        }
        // printClauses();
        configureSatSolver(solver);

        try {
            addClausesToSolver(solver);
            cleanUpClauses();

            this.solvingStart = System.currentTimeMillis();
            boolean isSatisfiable = solver.isSatisfiable();
            this.solvingEnd = System.currentTimeMillis();

            long solvingTime = solvingEnd - solvingStart;
            long serializationTime = serializationEnd - serializationStart;

            Statistics.addOrIncrementEntry("sat_serialization_time(ms)", serializationTime);
            Statistics.addOrIncrementEntry("sat_solving_time(ms)", solvingTime);

            if (isSatisfiable) {
                solutions = decode(solver.model());
            } else {
                System.out.println("Not solvable!");
                // Lazily initialize unsatisfiableConstraintExplainer when there is no solution
                unsatisfiableConstraintExplainer = new MaxSATUnsatisfiableConstraintExplainer();
            }

        } catch (ContradictionException e) {
            logger.warn("Contradiction exception: ", e);
            // This case indicates that constraints are not solvable, too. This is normal so continue
            // execution and let solver strategy to explain why there is no solution
            unsatisfiableConstraintExplainer = new MaxSATUnsatisfiableConstraintExplainer();
        } catch (Exception e) {
            throw new BugInCF("Unexpected error occurred!", e);
        }
        return solutions;
    }

    /**
     * Convert constraints to list of VecInt.
     */
    @Override
    public void encodeAllConstraints() {
        for (Constraint constraint : constraints) {
            collectVarSlots(constraint);
            VecInt[] encoding = constraint.serialize(formatTranslator);
            if (encoding == null) {
                logger.warn(getClass()
                        + " doesn't support encoding constraint: " + constraint
                        + "of class: " + constraint.getClass());
                continue;
            }
            for (VecInt res : encoding) {
                if (res != null && res.size() != 0) {
                    if (constraint instanceof PreferenceConstraint) {
                        softClauses.add(new Pair<VecInt, Integer>(res, ((PreferenceConstraint) constraint).getWeight()));
                    } else {
                        hardClauses.add(res);
                    }
                }
            }
        }
    }

    protected void encodeWellFormednessRestriction() {
        for (Integer varSlotId : varSlotIds) {
            formatTranslator.generateWellFormednessClauses(wellFormednessClauses, varSlotId);
        }
    }

    /**
     * sat solver configuration Configure
     *
     * @param solver
     */
    private void configureSatSolver(WeightedMaxSatDecorator solver) {

        final int totalVars = (slotManager.getSlots().size() * lattice.numTypes);
        final int totalClauses = hardClauses.size() + wellFormednessClauses.size() + softClauses.size();

        solver.newVar(totalVars);
        solver.setExpectedNumberOfClauses(totalClauses);
        Statistics.addOrIncrementEntry("cnf_clause_size", totalClauses);
        countVariables();
        solver.setTimeoutMs(1000000);
    }

    private void addClausesToSolver(WeightedMaxSatDecorator solver) throws ContradictionException {
        for (VecInt hardClause : hardClauses) {
            solver.addHardClause(hardClause);
        }

        for (VecInt wellFormednessClause: wellFormednessClauses) {
            solver.addHardClause(wellFormednessClause);
        }

        for (Pair<VecInt, Integer> softclause : softClauses) {
            solver.addSoftClause(softclause.b, softclause.a);
        }
    }

    private void cleanUpClauses() {
        hardClauses.clear();
        wellFormednessClauses.clear();
        softClauses.clear();
    }

    protected Map<Integer, Qualifier> decode(int[] solution) {
        Map<Integer, Qualifier> result = new HashMap<>();
        for (Integer var : solution) {
            if (var > 0) {
                var = var - 1;
                int slotId = MathUtils.getSlotId(var, lattice);
                Qualifier type = formatTranslator.decodeSolution(var, context);
                result.put(slotId, type);
            }
        }
        return result;
    }

    protected void countVariables() {

        Set<Integer> vars = new HashSet<Integer>();

        for (VecInt vi : hardClauses) {
            for (int i : vi.toArray()) {
                vars.add(i);
            }
        }
        Statistics.addOrIncrementEntry("cnf_variable_size", vars.size());
    }

    protected boolean shouldOutputCNF() {
        return solverOptions.getBoolArg(MaxSatSolverArg.outputCNF);
    }

    /**
     * Write CNF clauses into a string.
     */
    protected void buildCNFInput() {

        final int totalClauses = hardClauses.size()+ wellFormednessClauses.size();
        final int totalVars = slotManager.getSlots().size() * lattice.numTypes;

        CNFInput.append("c This is the CNF input\n");
        CNFInput.append("p cnf ");
        CNFInput.append(totalVars);
        CNFInput.append(" ");
        CNFInput.append(totalClauses);
        CNFInput.append("\n");

        for (VecInt hardClause : hardClauses) {
            buildCNFInputHelper(hardClause);
        }
        for (VecInt wellFormedNessClause: wellFormednessClauses) {
            buildCNFInputHelper(wellFormedNessClause);
        }
    }

    private void buildCNFInputHelper(VecInt clause) {
        int[] literals = clause.toArray();
        for (int i = 0; i < literals.length; i++) {
            CNFInput.append(literals[i]);
            CNFInput.append(" ");
        }
        CNFInput.append("0\n");
    }

    protected void writeCNFInput() {
        writeCNFInput("cnfdata.txt");
    }

    protected void writeCNFInput(String file) {
        FileUtils.writeFile(new File(CNFData.getAbsolutePath() + "/" + file), CNFInput.toString());
    }

    /**
     * print all soft and hard clauses for testing.
     */
    protected void printClauses() {
        System.out.println("Hard clauses: ");
        for (VecInt hardClause : hardClauses) {
            System.out.println(hardClause);
        }
        System.out.println();
        System.out.println("WellFormedness clauses: ");
        for (VecInt wellFormednessClause: wellFormednessClauses) {
            System.out.println(wellFormednessClause);
        }
        System.out.println();
        System.out.println("Soft clauses: ");
        for (Pair<VecInt, Integer> softclause : softClauses) {
            System.out.println(softclause.a + " w: " + softclause.b);
        }
    }

    @Override
    public Collection<Constraint> explainUnsatisfiable() {
        return unsatisfiableConstraintExplainer.minimumUnsatisfiableConstraints();
    }

    class MaxSATUnsatisfiableConstraintExplainer {

        /**
         * A mapping from VecInt to Constraint.
         * */
        private final Map<VecInt, Constraint> vecIntConstraintMap;

        /**
         * A mapping from IConstr to VecInt. IConstr is the result of adding VecInt to solver.
         */

        private final Map<IConstr, VecInt> iConstrVecIntMap;

        private MaxSATUnsatisfiableConstraintExplainer() {
            // Using IdentityHashMap because different VecInts can share the same hash code,
            // one VecInt might be overriden by a different VecInt.But VecInt has one-to-one
            // relation to Constraint. Therefore, IdentityHashMap is used.
            vecIntConstraintMap = new IdentityHashMap<>();
            iConstrVecIntMap = new IdentityHashMap<>();
            cleanUpClauses();
            // Fill up hardClauses and wellFormednessClauses again(cleared before) to feed into
            // explanation solver
            fillHardClauses();
            encodeWellFormednessRestriction();

        }

        // Compared to encodeAllConstrains(), this method doesn't format translate soft clauses,
        // and additionally stores the mapping from VecInt to Constraint, so that Constraint can
        // be reversly looked up by VecInt
        private void fillHardClauses() {
            // Fill up vecIntConstraintMap to reversely lookup Constraint from VecInt
            for (Constraint constraint : constraints) {
                VecInt[] encoding = constraint.serialize(formatTranslator);
                if (encoding == null) {
                    // Happens for unsupported Constraints. Already warned in encodeAllConstraints()
                    continue;
                }
                for (VecInt e : encoding) {
                    if (e != null && e.size() != 0 && !(constraint instanceof PreferenceConstraint)) {
                        hardClauses.add(e);
                        vecIntConstraintMap.put(e, constraint);
                    }
                }
            }
        }

        public Collection<Constraint> minimumUnsatisfiableConstraints() {
            // It's ok to use HashSet for Constraint, because its hashCose() implementation differentiates different
            // Constraints well.
            Set<Constraint> mus = new HashSet<>();
            // Explainer solver that is used
            Xplain<IPBSolver> explanationSolver = new Xplain<>(SolverFactory.newDefault());
            configureExplanationSolver(hardClauses, wellFormednessClauses, slotManager, lattice, explanationSolver);
            try {
                addClausesToExplanationSolver(explanationSolver);
                assert !explanationSolver.isSatisfiable();

                Collection<IConstr> explanation = explanationSolver.explain();

                for (IConstr i : explanation) {
                    VecInt vecInt = iConstrVecIntMap.get(i);
                    if (vecIntConstraintMap.get(vecInt) != null) {
                        // This case is reached if vecInt is from Constraint
                        mus.add(vecIntConstraintMap.get(vecInt));
                    } else {
                        // This case indicates vecInt is well-formedness restriction
                        // TODO Instead of printing it, can we have a dedicated type, e.g. WellFormednessConstraint <: Constraint
                        // TODO so that we can also add it to the result set?
                        System.out.println("Explanation hits well-formedness restriction: " + i);
                    }
                }
            } catch (Exception e) {
                throw new BugInCF("Explanation solver encountered not-expected exception: ", e);
            }
            return mus;
        }

        private void configureExplanationSolver(final List<VecInt> hardClauses, final List<VecInt> wellformedness,
                final SlotManager slotManager, final Lattice lattice, final Xplain<IPBSolver> explainer) {
            int numberOfNewVars = slotManager.getSlots().size() * lattice.numTypes;
            System.out.println("Number of variables: " + numberOfNewVars);
            int numberOfClauses = hardClauses.size() + wellformedness.size();
            System.out.println("Number of clauses: " + numberOfClauses);
            explainer.setMinimizationStrategy(new DeletionStrategy());
            explainer.newVar(numberOfNewVars);
            explainer.setExpectedNumberOfClauses(numberOfClauses);
        }

        private void addClausesToExplanationSolver(Xplain<IPBSolver> explanationSolver) throws ContradictionException {
            for (VecInt clause : hardClauses) {
                IConstr iConstr = explanationSolver.addClause(clause);
                iConstrVecIntMap.put(iConstr, clause);

            }
            for (VecInt clause : wellFormednessClauses) {
                IConstr iConstr = explanationSolver.addClause(clause);
                iConstrVecIntMap.put(iConstr, clause);
            }
        }
    }
}
