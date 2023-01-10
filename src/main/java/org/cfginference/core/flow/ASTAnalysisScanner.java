package org.cfginference.core.flow;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.TypeSystems;
import org.cfginference.core.event.Event;
import org.cfginference.core.event.EventListener;
import org.cfginference.core.event.EventManager;
import org.cfginference.core.model.constraint.Constraint;
import org.cfginference.core.model.constraint.ConstraintManager;
import org.cfginference.core.model.element.QualifiedElement;
import org.cfginference.core.model.location.QualifierLocation;
import org.cfginference.core.model.qualifier.AnnotationProxy;
import org.cfginference.core.model.slot.ProductSlot;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.slot.SlotManager;
import org.cfginference.core.model.slot.SourceSlot;
import org.cfginference.core.model.util.SlotLocator;
import org.cfginference.core.typesystem.TypeSystem;
import org.cfginference.util.JaifBuilder;
import org.cfginference.util.ProductSlotUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ASTAnalysisScanner extends TreePathScanner<Void, Void> implements EventListener {

    private static final Logger logger = LoggerFactory.getLogger(ASTAnalysisScanner.class);

    private final Context context;

    private final TypeSystems typeSystems;

    private final SlotLocator slotLocator;

    private final SlotManager slotManager;

    private final ConstraintManager constraintManager;

    private ASTAnalysisScanner(Context context) {
        this.context = context;
        this.typeSystems = TypeSystems.instance(context);
        this.slotLocator = SlotLocator.instance(context);
        this.slotManager = SlotManager.instance(context);
        this.constraintManager = ConstraintManager.instance(context);

        EventManager eventManager = EventManager.instance(context);
        eventManager.register(this);

        context.put(ASTAnalysisScanner.class, this);
    }

    public static ASTAnalysisScanner instance(Context context) {
        ASTAnalysisScanner instance = context.get(ASTAnalysisScanner.class);
        if (instance == null) {
            instance = new ASTAnalysisScanner(context);
        }
        return instance;
    }

    @Override
    public void finished(Event e) {
        if (e == Event.SimpleEvent.FULL_ANALYSIS) {
            printSlotLocationSummary();
            printConstraintsSummary();
            writeJaif();
        }
    }

    @Override
    public Void visitClass(ClassTree tree, Void unused) {
        locateSourceSlots(tree);
        typeCheckDeclaration(tree);
        return super.visitClass(tree, unused);
    }

    @Override
    public Void visitMethod(MethodTree tree, Void unused) {
        locateSourceSlots(tree);
        typeCheckDeclaration(tree);
        return super.visitMethod(tree, unused);
    }

    @Override
    public Void visitVariable(VariableTree tree, Void unused) {
        VariableElement variableElement = TreeUtils.elementFromDeclaration(tree);
        if (variableElement.getKind() != ElementKind.PARAMETER) {
            // params should be handled by visitMethod
            locateSourceSlots(tree);
        }
        typeCheckDeclaration(tree);
        return super.visitVariable(tree, unused);
    }

    void handleArtificialTrees(Map<Tree, TreePath> treeToPath) {
        for (Map.Entry<Tree, TreePath> e : treeToPath.entrySet()) {
            Tree tree = e.getKey();
            if (tree.getKind() == Tree.Kind.VARIABLE) {
                locateSourceSlots(tree);
            }
        }
    }

    private void locateSourceSlots(Tree tree) {
        Element element = Objects.requireNonNull(TreeUtils.elementFromTree(tree));
        QualifiedElement<ProductSlot> declType = getCombinedQualifiedElement(element);
        slotLocator.locateSourceSlots(declType, tree);
    }

    private QualifiedElement<ProductSlot> getCombinedQualifiedElement(Element element) {
        Iterator<TypeSystem> typeSystemsIter = typeSystems.get().iterator();
        QualifiedElement<ProductSlot> result = null;

        while (typeSystemsIter.hasNext()) {
            QualifiedElement<ProductSlot> next = typeSystemsIter.next().getDeclarationTypeResolver().getType(element);
            result = (result == null) ? next : ProductSlotUtils.combine(context, result, next);
        }
        return Objects.requireNonNull(result);
    }

    private void typeCheckDeclaration(Tree tree) {
        Element element = Objects.requireNonNull(TreeUtils.elementFromTree(tree));
        for (TypeSystem typeSystem : typeSystems.get()) {
            typeSystem.getTypeChecker().checkDeclaration(element, tree);
        }
    }

    private void printSlotLocationSummary() {
        logger.info("Slot Location Summary:");
        for (Map.Entry<Slot, QualifierLocation> entry: slotLocator.getLocations().entrySet()) {
            logger.info("{} ==> {}", entry.getKey(), entry.getValue());
        }
        logger.info("Unlocated Slots:");
        for (Slot slot : slotManager.getSlots()) {
            if (slot instanceof SourceSlot && slotLocator.getLocation(slot) == null) {
                logger.info(slot.toString());
            }
        }
        logger.info("End of Slot Location Summary");
    }

    private void printConstraintsSummary() {
        logger.info("Constraints Summary:");
        for (Constraint c : constraintManager.getEffectiveConstraints().values()) {
            logger.info(c.toString());
        }
        logger.info("End of Constraints Summary");
    }

    private void writeJaif() {
        Map<QualifierLocation, String> values = new HashMap<>();
        Set<Class<? extends Annotation>> annotationClasses = new HashSet<>();

        for (Slot slot : slotManager.getSlots()) {
            if (slot instanceof SourceSlot sourceSlot) {
                QualifierLocation q = slotLocator.getLocation(sourceSlot);
                if (q != null && q.isInsertable()) {
                    AnnotationProxy anno = sourceSlot.toAnnotation();
                    values.put(q, anno.toString());
                    annotationClasses.add(anno.getAnnotationClass());
                }
            }
        }

        JaifBuilder jb = new JaifBuilder(values, annotationClasses);
        logger.info("Jaif:");
        logger.info(jb.createJaif());
    }
}
