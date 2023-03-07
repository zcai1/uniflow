package org.uniflow.core.flow;

import com.sun.tools.javac.util.Context;
import org.uniflow.core.TypeSystems;
import org.uniflow.core.model.constraint.Constraint;
import org.uniflow.core.model.constraint.ConstraintManager;
import org.uniflow.core.model.slot.Slot;
import org.uniflow.core.model.slot.SlotManager;
import org.uniflow.core.model.util.SlotLocator;
import org.uniflow.core.typesystem.QualifierHierarchy;
import org.uniflow.core.typesystem.TypeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AfterAnalysis {

    private final Logger logger = LoggerFactory.getLogger(AfterAnalysis.class);

    private final Context context;

    private final TypeSystems typeSystems;

    private final SlotLocator slotLocator;

    private final SlotManager slotManager;

    private final ConstraintManager constraintManager;

    private AfterAnalysis(Context context) {
        this.context = context;
        this.typeSystems = TypeSystems.instance(context);
        this.slotLocator = SlotLocator.instance(context);
        this.slotManager = SlotManager.instance(context);
        this.constraintManager = ConstraintManager.instance(context);

        context.put(AfterAnalysis.class, this);
    }

    public static AfterAnalysis instance(Context context) {
        AfterAnalysis instance = context.get(AfterAnalysis.class);
        if (instance == null) {
            instance = new AfterAnalysis(context);
        }
        return instance;
    }

    public void start() {
        printSlotAndConstraintSummary();
    }

    private void printSlotAndConstraintSummary() {
        for (TypeSystem ts : typeSystems.get()) {
            for (QualifierHierarchy qualifierHierarchy : ts.getQualifierHierarchies()) {
                String sectionName = "Slot Summary";
                String qualifierHierarchyName = qualifierHierarchy.getClass().getSimpleName();
                logger.info("===== {} for {} =====", sectionName, qualifierHierarchyName);
                for (Slot slot : slotManager.getSlots()) {
                    assert slot.getOwner() == qualifierHierarchy;

                    logger.info("{}, location: {}", slot, slotLocator.getLocation(slot));
                }
                logger.info("===== End of {} for {} =====", sectionName, qualifierHierarchyName);

                sectionName = "Constraint Summary";
                logger.info("===== {} for {} =====", sectionName, qualifierHierarchyName);
                for (Constraint constraint : constraintManager.getEffectiveConstraints(qualifierHierarchy)) {
                    logger.info(constraint.toString());
                }
                logger.info("===== End of {} for {} =====", sectionName, qualifierHierarchyName);
            }
        }
    }
}
