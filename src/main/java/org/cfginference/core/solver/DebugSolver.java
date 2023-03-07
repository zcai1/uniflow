package org.cfginference.core.solver;

import com.sun.tools.javac.util.Context;
import org.cfginference.core.model.constraint.Constraint;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.util.SlotLocator;
import org.cfginference.core.typesystem.QualifierHierarchy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

public final class DebugSolver implements InferenceSolver {

    private static final Logger logger = LoggerFactory.getLogger(DebugSolver.class);

    private SlotLocator slotLocator;

    @Override
    public InferenceResult solve(Context context,
                                 Map<String, String> configuration,
                                 Collection<Slot> slots,
                                 Collection<Constraint> constraints,
                                 QualifierHierarchy qualifierHierarchy) {

        this.slotLocator = SlotLocator.instance(context);

        String sectionName = "Slot Summary";
        String qualifierHierarchyName = qualifierHierarchy.getClass().getSimpleName();
        logger.info("===== {} for {} =====", sectionName, qualifierHierarchyName);
        for (Slot slot : slots) {
            assert slot.getOwner() == qualifierHierarchy;

            logger.info("{}, location: {}", slot, slotLocator.getLocation(slot));
        }
        logger.info("===== End of {} for {} =====", sectionName, qualifierHierarchyName);

        sectionName = "Constraint Summary";
        logger.info("===== {} for {} =====", sectionName, qualifierHierarchyName);
        for (Constraint constraint : constraints) {
            logger.info(constraint.toString());
        }
        logger.info("===== End of {} for {} =====", sectionName, qualifierHierarchyName);

        return null;
    }
}
