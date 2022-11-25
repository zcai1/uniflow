package org.cfginference.core.flow;

import com.google.common.base.Verify;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.model.slot.ProductSlot;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.slot.SlotManager;
import org.cfginference.core.model.type.PrimaryQualifiedType;
import org.cfginference.core.model.type.QualifiedArrayType;
import org.cfginference.core.model.type.QualifiedDeclaredType;
import org.cfginference.core.model.type.QualifiedIntersectionType;
import org.cfginference.core.model.type.QualifiedNullType;
import org.cfginference.core.model.type.QualifiedPrimitiveType;
import org.cfginference.core.model.type.QualifiedType;
import org.cfginference.core.model.type.QualifiedUnionType;
import org.cfginference.core.model.util.QualifiedTypeCombiner;
import org.cfginference.core.model.util.QualifiedTypeModifier;
import org.cfginference.core.model.util.QualifiedTypeScanner;
import org.cfginference.core.typesystem.QualifierHierarchy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ProductSlotUtils {

    private static final Logger logger = LoggerFactory.getLogger(ProductSlotUtils.class);

    private static final ProductSlotHasHierarchies hasHierarchiesChecker = new ProductSlotHasHierarchies();
    
    private ProductSlotUtils() {
        // utils class
    }

    // Simple traverses two types of the same structure and merge each ProductSlot it encounters
    public static QualifiedType<ProductSlot> merge(Context context,
                                                   QualifiedType<ProductSlot> t1,
                                                   QualifiedType<ProductSlot> t2,
                                                   boolean lub) {
        QualifiedTypeCombiner<ProductSlot, ProductSlot, ProductSlot> merger = lub
                ? ProductSlotLubResolver.instance(context) : ProductSlotGlbResolver.instance(context);
        return merger.visit(t1, t2);
    }

    // Simple traverses two types of the same structure and applies changes on newType to
    // the oldType for the given hierarchies.
    public static QualifiedType<ProductSlot> replace(Context context,
                                                     QualifiedType<ProductSlot> oldType,
                                                     QualifiedType<ProductSlot> newType,
                                                     Set<QualifierHierarchy> forHierarchies) {
        ProductSlotReplacer replacer = ProductSlotReplacer.instance(context);
        replacer.setQualifierHierarchies(forHierarchies);
        QualifiedType<ProductSlot> result = replacer.visit(oldType, newType);
        replacer.setQualifierHierarchies(Collections.emptySet());
        return result;
    }

    // Traverse the type and remove any slots related to the given hierarchies
    public static QualifiedType<ProductSlot> remove(Context context,
                                                    QualifiedType<ProductSlot> type,
                                                    Set<QualifierHierarchy> forHierarchies) {
        ProductSlotRemover remover = ProductSlotRemover.instance(context);
        return remover.visit(type, forHierarchies);
    }

    public static boolean hasHierarchies(QualifiedType<ProductSlot> type, Set<QualifierHierarchy> hierarchies) {
        return hasHierarchiesChecker.scan(type, hierarchies);
    }

    private static final class ProductSlotRemover
            extends QualifiedTypeModifier<ProductSlot, ProductSlot, Set<QualifierHierarchy>> {

        private final SlotManager slotManager;

        private ProductSlotRemover(Context context) {
            slotManager = SlotManager.instance(context);

            context.put(ProductSlotRemover.class, this);
        }

        public static ProductSlotRemover instance(Context context) {
            ProductSlotRemover instance = context.get(ProductSlotRemover.class);
            if (instance == null) {
                instance = new ProductSlotRemover(context);
            }
            return instance;
        }

        @Override
        protected ProductSlot defaultAction(PrimaryQualifiedType<ProductSlot> type,
                                            Set<QualifierHierarchy> forHierarchies) {
            Map<QualifierHierarchy, Slot> result = new LinkedHashMap<>(type.getQualifier().getSlots());
            boolean changed = false;
            for (QualifierHierarchy hierarchy : forHierarchies) {
                Slot removedSlot = result.remove(hierarchy);
                changed = changed || removedSlot != null;
            }
            return changed ? slotManager.createProductSlot(result) : type.getQualifier();
        }
    }

    private static final class ProductSlotReplacer
            extends QualifiedTypeCombiner<ProductSlot, ProductSlot, ProductSlot> {

        private final SlotManager slotManager;

        private Set<QualifierHierarchy> qualifierHierarchies;

        private ProductSlotReplacer(Context context) {
            slotManager = SlotManager.instance(context);
            qualifierHierarchies = Collections.emptySet();

            context.put(ProductSlotReplacer.class, this);
        }

        public static ProductSlotReplacer instance(Context context) {
            ProductSlotReplacer instance = context.get(ProductSlotReplacer.class);
            if (instance == null) {
                instance = new ProductSlotReplacer(context);
            }
            return instance;
        }

        public void setQualifierHierarchies(Set<QualifierHierarchy> qualifierHierarchies) {
            this.qualifierHierarchies = qualifierHierarchies;
        }

        @Override
        protected ProductSlot defaultAction(PrimaryQualifiedType<ProductSlot> oldType,
                                            PrimaryQualifiedType<ProductSlot> newType) {
            Map<QualifierHierarchy, Slot> oldSlots = oldType.getQualifier().getSlots();
            Map<QualifierHierarchy, Slot> newSlots = newType.getQualifier().getSlots();
            Map<QualifierHierarchy, Slot> result = new LinkedHashMap<>(oldSlots);

            for (QualifierHierarchy hierarchy : qualifierHierarchies) {
                Slot newSlot = newSlots.get(hierarchy);
                if (newSlot == null) {
                    Slot removedSlot = result.remove(hierarchy);

                    if (removedSlot != null) {
                        logger.debug("Removing slot {} when replacing {} with {}", removedSlot, oldType, newType);
                    }
                } else {
                    result.put(hierarchy, newSlot);
                }
            }
            return slotManager.createProductSlot(result);
        }
    }

    private static final class ProductSlotGlbResolver 
            extends QualifiedTypeCombiner<ProductSlot, ProductSlot, ProductSlot> {

        private final SlotManager slotManager;

        private ProductSlotGlbResolver(Context context) {
            slotManager = SlotManager.instance(context);

            context.put(ProductSlotGlbResolver.class, this);
        }

        public static ProductSlotGlbResolver instance(Context context) {
            ProductSlotGlbResolver instance = context.get(ProductSlotGlbResolver.class);
            if (instance == null) {
                instance = new ProductSlotGlbResolver(context);
            }
            return instance;
        }

        @Override
        protected ProductSlot defaultAction(PrimaryQualifiedType<ProductSlot> type1,
                                            PrimaryQualifiedType<ProductSlot> type2) {
            Map<QualifierHierarchy, Slot> slots1 = type1.getQualifier().getSlots();
            Map<QualifierHierarchy, Slot> slots2 = type2.getQualifier().getSlots();
            Verify.verify(
                    slots1.keySet().equals(slots2.keySet()),
                    """
                    Failed to find the glb of two product slots,
                    qualified type #1: %s,
                    qualified type #2: %s
                    """,
                    type1,
                    type2
            );

            Map<QualifierHierarchy, Slot> glbSlots = new LinkedHashMap<>();
            for (Map.Entry<QualifierHierarchy, Slot> e : slots1.entrySet()) {
                QualifierHierarchy hierarchy = e.getKey();
                Slot s1 = e.getValue();
                Slot s2 = slots2.get(hierarchy);
                glbSlots.put(hierarchy, hierarchy.greatestLowerBound(s1, s2));
            }
            return slotManager.createProductSlot(glbSlots);
        }
    }

    private static final class ProductSlotLubResolver
            extends QualifiedTypeCombiner<ProductSlot, ProductSlot, ProductSlot> {

        private final SlotManager slotManager;

        private ProductSlotLubResolver(Context context) {
            slotManager = SlotManager.instance(context);

            context.put(ProductSlotLubResolver.class, this);
        }

        public static ProductSlotLubResolver instance(Context context) {
            ProductSlotLubResolver instance = context.get(ProductSlotLubResolver.class);
            if (instance == null) {
                instance = new ProductSlotLubResolver(context);
            }
            return instance;
        }

        @Override
        protected ProductSlot defaultAction(PrimaryQualifiedType<ProductSlot> type1,
                                            PrimaryQualifiedType<ProductSlot> type2) {
            Map<QualifierHierarchy, Slot> slots1 = type1.getQualifier().getSlots();
            Map<QualifierHierarchy, Slot> slots2 = type2.getQualifier().getSlots();
            Verify.verify(
                    slots1.keySet().equals(slots2.keySet()),
                    """
                    Failed to find the lub of two product slots,
                    qualified type #1: %s,
                    qualified type #2: %s
                    """,
                    type1,
                    type2
            );

            Map<QualifierHierarchy, Slot> lubSlots = new LinkedHashMap<>();
            for (Map.Entry<QualifierHierarchy, Slot> e : slots1.entrySet()) {
                QualifierHierarchy hierarchy = e.getKey();
                Slot s1 = e.getValue();
                Slot s2 = slots2.get(hierarchy);
                lubSlots.put(hierarchy, hierarchy.leastUpperBound(s1, s2));
            }
            return slotManager.createProductSlot(lubSlots);
        }
    }

    @Deprecated
    public static final class ProductSlotCombiner
            extends QualifiedTypeCombiner<ProductSlot, ProductSlot, ProductSlot> {

        private final SlotManager slotManager;

        private ProductSlotCombiner(Context context) {
            slotManager = SlotManager.instance(context);

            context.put(ProductSlotCombiner.class, this);
        }

        public static ProductSlotCombiner instance(Context context) {
            ProductSlotCombiner instance = context.get(ProductSlotCombiner.class);
            if (instance == null) {
                instance = new ProductSlotCombiner(context);
            }
            return instance;
        }

        @Override
        protected ProductSlot defaultAction(PrimaryQualifiedType<ProductSlot> type1,
                                            PrimaryQualifiedType<ProductSlot> type2) {
            Map<QualifierHierarchy, Slot> slots1 = type1.getQualifier().getSlots();
            Map<QualifierHierarchy, Slot> slots2 = type2.getQualifier().getSlots();
            Verify.verify(
                    Collections.disjoint(slots1.keySet(), slots2.keySet()),
                    """
                    Failed to combine two product slots because they are not disjoint,
                    qualified type #1: %s,
                    qualified type #2: %s
                    """,
                    type1,
                    type2
            );

            var combinedSlots = new LinkedHashMap<>(slots1);
            combinedSlots.putAll(slots2);
            return slotManager.createProductSlot(combinedSlots);
        }
    }

    private static class ProductSlotHasHierarchies extends ProductSlotOrChecker<Set<QualifierHierarchy>> {
        public ProductSlotHasHierarchies() {
            super(false);
        }

        @Override
        protected boolean defaultAction(PrimaryQualifiedType<ProductSlot> type, Set<QualifierHierarchy> hierarchies) {
            Set<QualifierHierarchy> hierarchiesInSlot = type.getQualifier().getSlots().keySet();
            return !hierarchiesInSlot.isEmpty() && !Collections.disjoint(hierarchies, hierarchiesInSlot);
        }
    }

    private static abstract class ProductSlotOrChecker<P> extends QualifiedTypeScanner<ProductSlot, Boolean, P> {

        protected ProductSlotOrChecker(boolean defaultValue) {
            super(defaultValue);
        }

        @Override
        protected Boolean scanAndReduce(QualifiedType<ProductSlot> type, P p, Boolean r) {
            return r || scan(type, p);
        }

        @Override
        protected Boolean scanAndReduce(Iterable<? extends QualifiedType<ProductSlot>> qualifiedTypes, P p, Boolean r) {
            return r || scan(qualifiedTypes, p);
        }

        @Override
        public final Boolean reduce(Boolean r1, Boolean r2) {
            throw new UnsupportedOperationException();
        }

        protected boolean defaultAction(PrimaryQualifiedType<ProductSlot> type, P p) {
            return DEFAULT_VALUE;
        }

        @Override
        public Boolean visitArray(QualifiedArrayType<ProductSlot> type, P p) {
            return defaultAction(type, p) || super.visitArray(type, p);
        }

        @Override
        public Boolean visitDeclared(QualifiedDeclaredType<ProductSlot> type, P p) {
            return defaultAction(type, p) || super.visitDeclared(type, p);
        }

        @Override
        public Boolean visitIntersection(QualifiedIntersectionType<ProductSlot> type, P p) {
            return defaultAction(type, p) || super.visitIntersection(type, p);
        }

        @Override
        public Boolean visitNull(QualifiedNullType<ProductSlot> type, P p) {
            return defaultAction(type, p) || super.visitNull(type, p);
        }

        @Override
        public Boolean visitPrimitive(QualifiedPrimitiveType<ProductSlot> type, P p) {
            return defaultAction(type, p) || super.visitPrimitive(type, p);
        }

        @Override
        public Boolean visitUnion(QualifiedUnionType<ProductSlot> type, P p) {
            return defaultAction(type, p) || super.visitUnion(type, p);
        }
    }
}
