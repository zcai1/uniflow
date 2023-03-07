package org.uniflow.util;

import com.google.common.base.Verify;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Sets;
import com.sun.tools.javac.util.Context;
import org.uniflow.core.flow.SlotQualifierHierarchy;
import org.uniflow.core.model.constraint.AlwaysTrueConstraint;
import org.uniflow.core.model.constraint.Constraint;
import org.uniflow.core.model.element.PrimaryQualifiedElement;
import org.uniflow.core.model.element.QualifiedElement;
import org.uniflow.core.model.slot.ProductSlot;
import org.uniflow.core.model.slot.Slot;
import org.uniflow.core.model.slot.SlotManager;
import org.uniflow.core.model.type.PrimaryQualifiedType;
import org.uniflow.core.model.type.QualifiedArrayType;
import org.uniflow.core.model.type.QualifiedDeclaredType;
import org.uniflow.core.model.type.QualifiedIntersectionType;
import org.uniflow.core.model.type.QualifiedNullType;
import org.uniflow.core.model.type.QualifiedPrimitiveType;
import org.uniflow.core.model.type.QualifiedType;
import org.uniflow.core.model.type.QualifiedUnionType;
import org.uniflow.core.model.util.QualifiedElementCombiner;
import org.uniflow.core.model.util.QualifiedTypeCombiner;
import org.uniflow.core.model.util.QualifiedTypeModifier;
import org.uniflow.core.model.util.QualifiedTypeScanner;
import org.uniflow.core.typesystem.QualifierHierarchy;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ProductSlotUtils {

    private static final Logger logger = LoggerFactory.getLogger(ProductSlotUtils.class);

    private static final ProductSlotHasHierarchies hasHierarchiesChecker = new ProductSlotHasHierarchies();
    
    private ProductSlotUtils() {
        // utils class
    }

    public static QualifiedElement<ProductSlot> combine(Context context,
                                                        QualifiedElement<ProductSlot> e1,
                                                        QualifiedElement<ProductSlot> e2) {
        ProductSlotElementCombiner combiner = ProductSlotElementCombiner.instance(context);
        return combiner.visit(e1, e2);
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
        try {
            replacer.setQualifierHierarchies(forHierarchies);
            QualifiedType<ProductSlot> result = replacer.visit(oldType, newType);
            return result;
        } finally {
            replacer.setQualifierHierarchies(Collections.emptySet());
        }
    }

    // Traverse the type and remove any slots related to the given hierarchies
    public static QualifiedType<ProductSlot> remove(Context context,
                                                    QualifiedType<ProductSlot> type,
                                                    Set<QualifierHierarchy> forHierarchies) {
        ProductSlotRemover remover = ProductSlotRemover.instance(context);
        return remover.visit(type, forHierarchies);
    }

    public static QualifiedType<ProductSlot> refine(Context context,
                                                    QualifiedType<ProductSlot> oldType,
                                                    QualifiedType<ProductSlot> newType,
                                                    IncomparableSlotResolver resolver) {
        ProductSlotRefiner refiner = ProductSlotRefiner.instance(context);
        try {
            refiner.setResolver(resolver);
            QualifiedType<ProductSlot> result = refiner.visit(oldType, newType);
            return result;
        } finally {
            refiner.setResolver(null);
        }
    }

    public static QualifiedType<ProductSlot> refine(Context context,
                                                    QualifiedType<ProductSlot> oldType,
                                                    QualifiedType<ProductSlot> newType) {
        return refine(context, oldType, newType, IncomparableSlotResolver.ALWAYS_FIRST);
    }

    public static boolean hasHierarchies(QualifiedType<ProductSlot> type, Set<QualifierHierarchy> hierarchies) {
        return hasHierarchiesChecker.scan(type, hierarchies);
    }

    public static boolean fromSameHierarchies(ProductSlot s1, ProductSlot p2) {
        return Sets.symmetricDifference(s1.getSlots().keySet(), p2.getSlots().keySet())
                .isEmpty();
    }

    public static ProductSlot mergePrimaries(Context context,
                                             List<QualifiedType<ProductSlot>> types,
                                             Set<QualifierHierarchy> forHierarchies,
                                             boolean isLub) {
        SlotQualifierHierarchy slotQualifierHierarchy = SlotQualifierHierarchy.instance(context);
        SlotManager slotManager = SlotManager.instance(context);
        ArrayListMultimap<QualifierHierarchy, Slot> typesByHierarchy = ArrayListMultimap.create();

        // TODO: what if some/all types don't have primary qualifier?
        for (QualifiedType<ProductSlot> type : types) {
            if (type instanceof PrimaryQualifiedType<ProductSlot> pqType) {
                for (QualifierHierarchy hierarchy : forHierarchies) {
                    Slot slot = Objects.requireNonNull(pqType.getQualifier().getSlotByHierarchy(hierarchy));
                    typesByHierarchy.put(hierarchy, slot);
                }
            }
        }

        Map<QualifierHierarchy, Slot> result = new LinkedHashMap<>();
        for (QualifierHierarchy hierarchy : forHierarchies) {
            List<Slot> slots = typesByHierarchy.get(hierarchy);
            Slot mergedSlot = isLub
                    ? slotQualifierHierarchy.leastUpperBound(slots)
                    : slotQualifierHierarchy.greatestLowerBound(slots);
            result.put(hierarchy, mergedSlot);
        }
        return slotManager.createProductSlot(result);
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
        protected ProductSlot getQualifier(PrimaryQualifiedType<ProductSlot> oldType,
                                           PrimaryQualifiedType<ProductSlot> newType) {
            Map<QualifierHierarchy, ? extends Slot> oldSlots = oldType.getQualifier().getSlots();
            Map<QualifierHierarchy, ? extends Slot> newSlots = newType.getQualifier().getSlots();
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

        private final SlotQualifierHierarchy slotQualifierHierarchy;

        private ProductSlotGlbResolver(Context context) {
            slotManager = SlotManager.instance(context);
            slotQualifierHierarchy = SlotQualifierHierarchy.instance(context);

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
        protected ProductSlot getQualifier(PrimaryQualifiedType<ProductSlot> type1,
                                           PrimaryQualifiedType<ProductSlot> type2) {
            Map<QualifierHierarchy, ? extends Slot> slots1 = type1.getQualifier().getSlots();
            Map<QualifierHierarchy, ? extends Slot> slots2 = type2.getQualifier().getSlots();
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
            for (Map.Entry<QualifierHierarchy, ? extends Slot> e : slots1.entrySet()) {
                QualifierHierarchy hierarchy = e.getKey();
                Slot s1 = e.getValue();
                Slot s2 = slots2.get(hierarchy);
                glbSlots.put(hierarchy, slotQualifierHierarchy.greatestLowerBound(s1, s2));
            }
            return slotManager.createProductSlot(glbSlots);
        }
    }

    private static final class ProductSlotLubResolver
            extends QualifiedTypeCombiner<ProductSlot, ProductSlot, ProductSlot> {

        private final SlotManager slotManager;

        private final SlotQualifierHierarchy slotQualifierHierarchy;

        private ProductSlotLubResolver(Context context) {
            slotManager = SlotManager.instance(context);
            slotQualifierHierarchy = SlotQualifierHierarchy.instance(context);

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
        protected ProductSlot getQualifier(PrimaryQualifiedType<ProductSlot> type1,
                                           PrimaryQualifiedType<ProductSlot> type2) {
            Map<QualifierHierarchy, ? extends Slot> slots1 = type1.getQualifier().getSlots();
            Map<QualifierHierarchy, ? extends Slot> slots2 = type2.getQualifier().getSlots();
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
            for (Map.Entry<QualifierHierarchy, ? extends Slot> e : slots1.entrySet()) {
                QualifierHierarchy hierarchy = e.getKey();
                Slot s1 = e.getValue();
                Slot s2 = slots2.get(hierarchy);
                lubSlots.put(hierarchy, slotQualifierHierarchy.leastUpperBound(s1, s2));
            }
            return slotManager.createProductSlot(lubSlots);
        }
    }

    private static final class ProductSlotTypeCombiner
            extends QualifiedTypeCombiner<ProductSlot, ProductSlot, ProductSlot> {

        private final SlotManager slotManager;

        private ProductSlotTypeCombiner(Context context) {
            slotManager = SlotManager.instance(context);

            context.put(ProductSlotTypeCombiner.class, this);
        }

        public static ProductSlotTypeCombiner instance(Context context) {
            ProductSlotTypeCombiner instance = context.get(ProductSlotTypeCombiner.class);
            if (instance == null) {
                instance = new ProductSlotTypeCombiner(context);
            }
            return instance;
        }

        @Override
        protected ProductSlot getQualifier(PrimaryQualifiedType<ProductSlot> type1,
                                           PrimaryQualifiedType<ProductSlot> type2) {
            Map<QualifierHierarchy, ? extends Slot> slots1 = type1.getQualifier().getSlots();
            Map<QualifierHierarchy, ? extends Slot> slots2 = type2.getQualifier().getSlots();
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

            LinkedHashMap<QualifierHierarchy, Slot> combinedSlots = new LinkedHashMap<>(slots1);
            combinedSlots.putAll(slots2);
            return slotManager.createProductSlot(combinedSlots);
        }
    }

    private static final class ProductSlotElementCombiner
            extends QualifiedElementCombiner<ProductSlot, ProductSlot, ProductSlot> {

        private final SlotManager slotManager;

        private final ProductSlotTypeCombiner typeCombiner;

        private ProductSlotElementCombiner(Context context) {
            slotManager = SlotManager.instance(context);
            typeCombiner = ProductSlotTypeCombiner.instance(context);

            context.put(ProductSlotElementCombiner.class, this);
        }

        public static ProductSlotElementCombiner instance(Context context) {
            ProductSlotElementCombiner instance = context.get(ProductSlotElementCombiner.class);
            if (instance == null) {
                instance = new ProductSlotElementCombiner(context);
            }
            return instance;
        }

        @Override
        protected ProductSlot getQualifier(PrimaryQualifiedElement<ProductSlot> element1,
                                           PrimaryQualifiedElement<ProductSlot> element2) {
            Map<QualifierHierarchy, ? extends Slot> slots1 = element1.getQualifier().getSlots();
            Map<QualifierHierarchy, ? extends Slot> slots2 = element2.getQualifier().getSlots();
            Verify.verify(
                    Collections.disjoint(slots1.keySet(), slots2.keySet()),
                    """
                    Failed to combine two product slots because they are not disjoint,
                    qualified element #1: %s,
                    qualified element #2: %s
                    """,
                    element1,
                    element2
            );

            LinkedHashMap<QualifierHierarchy, Slot> combinedSlots = new LinkedHashMap<>(slots1);
            combinedSlots.putAll(slots2);
            return slotManager.createProductSlot(combinedSlots);
        }

        @Override
        protected QualifiedType<ProductSlot> getQualifiedType(QualifiedType<ProductSlot> type1,
                                                              QualifiedType<ProductSlot> type2) {
            return typeCombiner.visit(type1, type2);
        }
    }

    public interface IncomparableSlotResolver {
        IncomparableSlotResolver ALWAYS_FIRST = ((slot1, slot2, javaType) -> Objects.requireNonNull(slot1));
        IncomparableSlotResolver ALWAYS_SECOND = ((slot1, slot2, javaType) -> Objects.requireNonNull(slot2));

        Slot resolve(@Nullable Slot slot1, @Nullable Slot slot2, TypeMirror javaType);
    }

    private static final class ProductSlotRefiner
            extends QualifiedTypeCombiner<ProductSlot, ProductSlot, ProductSlot> {

        private IncomparableSlotResolver resolver;

        private final SlotQualifierHierarchy slotQualifierHierarchy;

        private final SlotManager slotManager;

        private ProductSlotRefiner(Context context) {
            slotManager = SlotManager.instance(context);
            slotQualifierHierarchy = SlotQualifierHierarchy.instance(context);

            context.put(ProductSlotRefiner.class, this);
        }

        public static ProductSlotRefiner instance(Context context) {
            ProductSlotRefiner instance = context.get(ProductSlotRefiner.class);
            if (instance == null) {
                instance = new ProductSlotRefiner(context);
            }
            return instance;
        }

        public void setResolver(IncomparableSlotResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        protected ProductSlot getQualifier(PrimaryQualifiedType<ProductSlot> type1,
                                           PrimaryQualifiedType<ProductSlot> type2) {
            Map<QualifierHierarchy, Slot> result = new LinkedHashMap<>();
            for (Map.Entry<QualifierHierarchy, ? extends Slot> e : type1.getQualifier().getSlots().entrySet()) {
                QualifierHierarchy hierarchy = e.getKey();
                Slot slot1 = e.getValue();
                Slot slot2 = type2.getQualifier().getSlots().get(hierarchy);
                Slot moreSpecificSlot = moreSpecificOf(slot1, slot2);
                if (moreSpecificSlot == null) {
                    moreSpecificSlot = resolver.resolve(slot1, slot2, type1.getJavaType());
                }
                result.put(hierarchy, moreSpecificSlot);
            }
            for (var e : type2.getQualifier().getSlots().entrySet()) {
                QualifierHierarchy hierarchy = e.getKey();
                if (!type1.getQualifier().getSlots().containsKey(hierarchy)) {
                    result.put(hierarchy, e.getValue());
                }
            }
            return slotManager.createProductSlot(result);
        }

        private @Nullable Slot moreSpecificOf(@Nullable Slot s1, @Nullable Slot s2) {
            if (s1 == null) {
                return s2;
            } else if (s2 == null) {
                return s1;
            }

            Constraint subtypeConstraint = slotQualifierHierarchy.getSubtypeConstraint(s1, s2);
            if (subtypeConstraint instanceof AlwaysTrueConstraint) {
                return s1;
            }

            subtypeConstraint = slotQualifierHierarchy.getSubtypeConstraint(s2, s1);
            if (subtypeConstraint instanceof AlwaysTrueConstraint) {
                return s2;
            }
            return null;
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
        protected Boolean scanAndReduce(Collection<? extends QualifiedType<ProductSlot>> qualifiedTypes, P p, Boolean r) {
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
