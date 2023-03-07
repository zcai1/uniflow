package org.cfginference.core.flow;

import com.google.common.collect.SetMultimap;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.sun.tools.javac.resources.CompilerProperties.Errors;
import com.sun.tools.javac.resources.CompilerProperties.Notes;
import com.sun.tools.javac.resources.CompilerProperties.Warnings;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticFlag;
import com.sun.tools.javac.util.Log;
import org.cfginference.core.PluginOptions;
import org.cfginference.core.TypeSystems;
import org.cfginference.core.model.constraint.AlwaysFalseConstraint;
import org.cfginference.core.model.constraint.Constraint;
import org.cfginference.core.model.constraint.ConstraintManager;
import org.cfginference.core.model.location.QualifierLocation;
import org.cfginference.core.model.qualifier.AnnotationProxy;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.reporting.AnalysisMessage;
import org.cfginference.core.model.reporting.PluginError;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.slot.SlotManager;
import org.cfginference.core.model.slot.SourceSlot;
import org.cfginference.core.model.util.SlotLocator;
import org.cfginference.core.solver.DefaultInferenceResult;
import org.cfginference.core.solver.InferenceResult;
import org.cfginference.core.solver.backend.Solver;
import org.cfginference.core.solver.backend.maxsat.MaxSatSolverFactory;
import org.cfginference.core.solver.frontend.LatticeBuilder;
import org.cfginference.core.solver.frontend.TwoQualifiersLattice;
import org.cfginference.core.solver.util.SolverOptions;
import org.cfginference.core.typesystem.QualifierHierarchy;
import org.cfginference.core.typesystem.TypeSystem;
import org.cfginference.util.JaifApplier;
import org.cfginference.util.JaifBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public final class SolveConstraints {

    private static final Logger logger = LoggerFactory.getLogger(SolveConstraints.class);

    private final Context context;

    private final PluginOptions options;

    private final TypeSystems typeSystems;

    private final SlotLocator slotLocator;

    private final SlotManager slotManager;

    private final ConstraintManager constraintManager;

    private final Properties properties;

    private final MaxSatSolverFactory maxSatSolverFactory;

    private final AlwaysFalseConstraint alwaysFalseConstraint;

    private final Log log;
    private SolveConstraints(Context context) {
        this.context = context;
        this.options = PluginOptions.instance(context);
        this.typeSystems = TypeSystems.instance(context);
        this.slotLocator = SlotLocator.instance(context);
        this.slotManager = SlotManager.instance(context);
        this.constraintManager = ConstraintManager.instance(context);
        this.maxSatSolverFactory = MaxSatSolverFactory.instance(context);
        this.log = Log.instance(context);

        this.properties = loadProperties();
        this.alwaysFalseConstraint = AlwaysFalseConstraint.instance();

        context.put(SolveConstraints.class, this);
    }

    public static SolveConstraints instance(Context context) {
        SolveConstraints instance = context.get(SolveConstraints.class);
        if (instance == null) {
            instance = new SolveConstraints(context);
        }
        return instance;
    }

    public void start() {
        if (options.getMode().isInference()) {
            onInference();
        } else {
            onTypeCheck();
        }
    }

    // TODO: consider a more flexible way of loading properties
    private Properties loadProperties() {
        Properties p = new Properties();
        try {
            p.load(Resources.getResource("messages.properties").openStream());
        } catch (IOException e) {
            throw new PluginError(e);
        }
        return p;
    }

    private void onTypeCheck() {
        // TODO: do we need to use solver?
        SetMultimap<QualifierHierarchy, Constraint> allConstraints = constraintManager.getEffectiveConstraints();
        for (TypeSystem ts : typeSystems.get()) {
            logger.info("Solving constraints for {}", ts.getClass().getSimpleName());
            for (QualifierHierarchy q : ts.getQualifierHierarchies()) {
                for (Constraint c : allConstraints.get(q)) {
                    if (c == alwaysFalseConstraint) {
                        reportUnsatConstraint(q, c);
                    } else {
                        logger.warn("Found variable constraint {} for QualifierHierarchy {}", c, q);
                    }
                }
            }
        }
    }

    private void onInference() {
        Map<QualifierLocation, String> values = new LinkedHashMap<>();
        Set<Class<? extends Annotation>> annotationClasses = new LinkedHashSet<>();
        boolean satisfiable = true;

        for (TypeSystem typeSystem : typeSystems.get()) {
            for (QualifierHierarchy qualifierHierarchy : typeSystem.getQualifierHierarchies()) {
                // solve
                InferenceResult inferenceResult = solveForInference(qualifierHierarchy);
                if (!inferenceResult.hasSolution()) {
                    satisfiable = false;
                    logger.error("Inference for {} was unsat!", qualifierHierarchy.getClass().getSimpleName());

                    for (Constraint unsatConstraint : inferenceResult.getUnsatisfiableConstraints()) {
                        reportUnsatConstraint(qualifierHierarchy, unsatConstraint);
                    }
                    continue;
                }

                // collect solutions to write jaif
                Map<Integer, Qualifier> solutions = inferenceResult.getSolutions();
                for (Slot s : slotManager.getSlots()) {
                    if (s instanceof SourceSlot && s.getOwner() == qualifierHierarchy) {
                        QualifierLocation location = slotLocator.getLocation(s);
                        if (location != null && location.isInsertable()) {
                            Qualifier solution = solutions.get(s.getId());
                            if (solution == null) continue;

                            AnnotationProxy anno = solution.toAnnotation();
                            values.put(location, anno.toString());
                            annotationClasses.add(anno.getAnnotationClass());
                        }
                    }
                }
            }
        }

        if (satisfiable) {
            JaifBuilder jb = new JaifBuilder(values, annotationClasses);
            writeJaif(jb.createJaif());

            if (options.getAfuOutputDir() != null) {
                JaifApplier.apply(context);
            }
        }
    }

    private void writeJaif(String content) {
        String outputPath = Objects.requireNonNull(options.getJaifOutputPath());
        File outputFile = new File(outputPath);
        try {
            Files.asCharSink(outputFile, StandardCharsets.UTF_8).write(content);
        } catch (IOException e) {
            throw new PluginError(e);
        }
    }

    private InferenceResult solveForInference(QualifierHierarchy qualifierHierarchy) {
        // TODO: support general solvers
        boolean hasAlwaysFalse = false;
        SetMultimap<QualifierHierarchy, Constraint> allConstraints = constraintManager.getEffectiveConstraints();
        Set<Constraint> constraints = allConstraints.get(qualifierHierarchy);
        if (constraints.contains(alwaysFalseConstraint)) {
            hasAlwaysFalse = true;
            constraints = new LinkedHashSet<>(constraints);
            constraints.remove(alwaysFalseConstraint);
        }

        Set<Slot> slots = slotManager.getSlots().stream().filter(s -> s.getOwner() == qualifierHierarchy)
                .collect(Collectors.toSet());
        SolverOptions solverOptions = new SolverOptions(Collections.emptyMap());
        TwoQualifiersLattice lattice = new LatticeBuilder().buildTwoTypeLattice(qualifierHierarchy,
                qualifierHierarchy.getTopQualifier(),
                qualifierHierarchy.getBottomQualifier());

        Solver<?> solver = maxSatSolverFactory.createSolver(solverOptions, slots, constraints, lattice);
        Map<Integer, Qualifier> solutions = solver.solve();
        if (solutions != null && !hasAlwaysFalse) {
            return new DefaultInferenceResult(solutions);
        } else {
            Set<Constraint> unsatConstraints = new LinkedHashSet<>();
            if (solutions == null) {
                unsatConstraints.addAll(solver.explainUnsatisfiable());
            }
            if (hasAlwaysFalse) {
                unsatConstraints.add(alwaysFalseConstraint);
            }
            return new DefaultInferenceResult(unsatConstraints);
        }
        // MaxSat2TypeSolver solver = new MaxSat2TypeSolver();
        //
        //
        // InferenceResult result = solver.solve(context,
        //         Collections.emptyMap(),
        //         ,
        //         constraints,
        //         qualifierHierarchy);
        // return result;
    }

    private void reportUnsatConstraint(QualifierHierarchy q, Constraint c) {
        Set<AnalysisMessage> unsatMessages = constraintManager.getUnsatMessages(q, c);
        if (unsatMessages.isEmpty()) {
            logger.debug("Found false constraint without any messages with {}", q.getClass().getSimpleName());
            return;
        }

        for (AnalysisMessage msg : unsatMessages) {
            reportMessage(msg);
        }
    }

    private void reportMessage(AnalysisMessage msg) {
        JavaFileObject oldSource = null;
        JavaFileObject newSource = null;

        Object msgSrc = msg.getSource();
        if (msgSrc instanceof JavaFileObject fileObjectSrc) {
            newSource = fileObjectSrc;
            oldSource = log.useSource(newSource);
        }

        try {
            String formattedMsg = msg.getFormattedMessage(properties);
            switch (msg.getKind()) {
                case ERROR -> log.error(DiagnosticFlag.API, msg.getPosition(), Errors.ProcMessager(formattedMsg));
                case WARNING -> log.warning(msg.getPosition(), Warnings.ProcMessager(formattedMsg));
                case MANDATORY_WARNING -> log.mandatoryWarning(msg.getPosition(), Warnings.ProcMessager(formattedMsg));
                default -> log.note(msg.getPosition(), Notes.ProcMessager(formattedMsg));
            }
        } finally {
            if (newSource != null) {
                log.useSource(oldSource);
            }
        }
    }
}
