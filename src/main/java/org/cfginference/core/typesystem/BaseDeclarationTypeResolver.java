package org.cfginference.core.typesystem;

import com.google.common.collect.ImmutableSet;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.PluginOptions;
import org.cfginference.core.event.Event;
import org.cfginference.core.event.EventListener;
import org.cfginference.core.event.EventManager;
import org.cfginference.core.model.element.QualifiedElement;
import org.cfginference.core.model.element.QualifiedExecutableElement;
import org.cfginference.core.model.element.QualifiedTypeParameterElement;
import org.cfginference.core.model.element.QualifiedVariableElement;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.slot.ConstantSlot;
import org.cfginference.core.model.slot.ProductSlot;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.slot.SlotManager;
import org.cfginference.core.model.slot.SourceSlot;
import org.cfginference.core.model.type.QualifiedType;
import org.cfginference.core.model.util.QualifiedElementBuilder;
import org.cfginference.core.model.util.QualifiedElementsCache;
import org.cfginference.core.model.util.QualifiedTypeBuilder;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.Pair;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: verify generated type
// NOTE: DeclarationTypeResolver should avoid determining qualifier location since the element
//  may not have been analyzed.
public class BaseDeclarationTypeResolver extends QualifiedElementBuilder<ProductSlot, Void>
        implements DeclarationTypeResolver, EventListener {

    protected final SlotManager slotManager;

    protected final BaseDeclarationQualifiedTypeBuilder qualifiedTypeBuilder;

    protected final PluginOptions options;

    protected final QualifiedElementsCache<ProductSlot> cache;

    // cache results for recursive getType invocation
    protected final IdentityHashMap<Element, ProductSlot> visitedElements;

    protected int getTypeCounter;

    protected final Set<QualifierHierarchy> qualifierHierarchies;

    protected boolean outermostElementFromSource;

    protected Element currentElement;

    public BaseDeclarationTypeResolver(Context context, TypeSystem typeSystem) {
        this.slotManager = SlotManager.instance(context);
        this.options = PluginOptions.instance(context);
        this.cache = new QualifiedElementsCache<>();
        this.visitedElements = new IdentityHashMap<>();
        this.getTypeCounter = 0;
        this.qualifierHierarchies = ImmutableSet.copyOf(typeSystem.getQualifierHierarchies());
        this.outermostElementFromSource = false;
        this.currentElement = null;

        this.qualifiedTypeBuilder = createQualifiedTypeBuilder();

        EventManager eventManager = EventManager.instance(context);
        eventManager.register(this);
    }

    protected BaseDeclarationQualifiedTypeBuilder createQualifiedTypeBuilder() {
        return new BaseDeclarationQualifiedTypeBuilder();
    }

    @Override
    public void finished(Event event) {
        if (event instanceof Event.NewAnalysisTask) {
            cache.clearLocalElements();
        }
    }

    @Override
    public QualifiedElement<ProductSlot> getType(Element element) {
        // TODO: consider inheritance (e.g., class & method annotations)
        boolean prevOutermostElementFromSource = outermostElementFromSource;
        try {
            ++getTypeCounter;
            outermostElementFromSource = ElementUtils.isElementFromSourceCode(element);
            return visit(element, null);
        } finally {
            outermostElementFromSource = prevOutermostElementFromSource;
            --getTypeCounter;

            if (getTypeCounter == 0) {
                visitedElements.clear();
            }
        }
    }

    @Override
    public QualifiedElement<ProductSlot> visit(Element element, Void unused) {
        Element prevElement = currentElement;
        currentElement = element;

        try {
            QualifiedElement<ProductSlot> cachedResult = cache.load(element);
            if (cachedResult != null) {
                return cachedResult;
            }

            QualifiedElement<ProductSlot> result = super.visit(element, unused);
            cache.save(result);
            return result;
        } finally {
            currentElement = prevElement;
        }

    }

    @Override
    protected QualifiedExecutableElement.Builder<ProductSlot> defaultBuilder(ExecutableElement element, Void unused) {
        TypeElement enclosingType = ElementUtils.enclosingTypeElement(element);
        TypeMirror returnType = element.getReturnType();
        TypeMirror receiverType = element.getReceiverType();

        // correct return type for constructor
        if (element.getKind() == ElementKind.CONSTRUCTOR) {
            returnType = enclosingType.asType();
        }
        // correct receiver type for methods without explicit receiver declaration
        if (receiverType.getKind() == TypeKind.NONE && ElementUtils.hasReceiver(element)) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                // constructor of an inner class
                enclosingType = ElementUtils.enclosingTypeElement(element.getEnclosingElement());
            }
            receiverType = enclosingType.asType();
        }

        List<QualifiedTypeParameterElement<ProductSlot>> typeParams = element.getTypeParameters().stream()
                .map(tp -> (QualifiedTypeParameterElement<ProductSlot>) visit(tp, unused))
                .toList();
        List<QualifiedVariableElement<ProductSlot>> params = element.getParameters().stream()
                .map(param -> (QualifiedVariableElement<ProductSlot>) visit(param, unused))
                .toList();
        List<QualifiedType<ProductSlot>> thrownTypes = element.getThrownTypes().stream()
                .map(t -> getQualifiedType(t, element, unused))
                .toList();

        return QualifiedExecutableElement.<ProductSlot>builder()
                .setJavaElement(element)
                .setTypeParameters(typeParams)
                .setReturnType(getQualifiedType(returnType, element, unused))
                .setParameters(params)
                .setReceiverType(getQualifiedType(receiverType, element, unused))
                .setThrownTypes(thrownTypes);
    }

    @Override
    protected QualifiedType<ProductSlot> getQualifiedType(TypeMirror type, Element element, Void unused) {
        return qualifiedTypeBuilder.visit(type);
    }

    @Override
    protected ProductSlot getQualifier(Element element, Void unused) {
        ProductSlot cachedResult = visitedElements.get(element);
        if (cachedResult != null) {
            return cachedResult;
        }

        Map<QualifierHierarchy, Slot> slots = new LinkedHashMap<>();
        for (QualifierHierarchy hierarchy : qualifierHierarchies) {
            Pair<ConstantSlot, Boolean> defaultSlot = getConstantDefault(element, hierarchy);
            if (defaultSlot.second || shouldAvoidSourceSlot()) {
                slots.put(hierarchy, defaultSlot.first);
            } else {
                SourceSlot sourceSlot = slotManager.createSourceSlot(hierarchy, defaultSlot.first);
                slots.put(hierarchy, sourceSlot);
            }
        }

        ProductSlot result = slotManager.createProductSlot(slots);
        visitedElements.put(element, result);
        return result;
    }

    // return ConstantSlot and a boolean representing whether the qualifier is fixed (from source)
    protected Pair<ConstantSlot, Boolean> getConstantDefault(Element element, QualifierHierarchy qualifierHierarchy) {
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            Qualifier q = qualifierHierarchy.getQualifier(am);
            if (q != null) {
                return Pair.of(slotManager.createConstantSlot(qualifierHierarchy, q), true);
            }
        }
        return Pair.of(slotManager.createConstantSlot(qualifierHierarchy, qualifierHierarchy.getTopQualifier()), false);
    }

    protected boolean shouldAvoidSourceSlot() {
        return !outermostElementFromSource || !options.getMode().isInference();
    }

    protected class BaseDeclarationQualifiedTypeBuilder extends QualifiedTypeBuilder<ProductSlot, Void> {

        @Override
        protected ProductSlot getQualifier(TypeMirror type, Void unused) {
            Map<QualifierHierarchy, Slot> slots = new LinkedHashMap<>();
            for (QualifierHierarchy hierarchy : qualifierHierarchies) {
                Pair<ConstantSlot, Boolean> defaultSlot = getConstantDefault(type, hierarchy);
                if (defaultSlot.second || shouldAvoidSourceSlot()) {
                    slots.put(hierarchy, defaultSlot.first);
                } else {
                    SourceSlot sourceSlot = slotManager.createSourceSlot(hierarchy, defaultSlot.first);
                    slots.put(hierarchy, sourceSlot);
                }
            }
            return slotManager.createProductSlot(slots);
        }

        // return ConstantSlot and a boolean representing whether the qualifier is fixed (from source)
        protected Pair<ConstantSlot, Boolean> getConstantDefault(TypeMirror type, QualifierHierarchy hierarchy) {
            for (AnnotationMirror am : type.getAnnotationMirrors()) {
                Qualifier q = hierarchy.getQualifier(am);
                if (q != null) {
                    return Pair.of(slotManager.createConstantSlot(hierarchy, q), true);
                }
            }

            ConstantSlot unannotatedDefault = null;
            if (type.getKind() == TypeKind.DECLARED && shouldLookupTypeElementQualifiers((DeclaredType) type)) {
                DeclaredType declaredType = (DeclaredType) type;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                Slot declQualifier;
                if (visitedElements.containsKey(typeElement)) {
                    declQualifier = visitedElements.get(typeElement).getSlotByHierarchy(hierarchy);
                } else {
                    declQualifier = getType(typeElement).getQualifier().getSlotByHierarchy(hierarchy);
                }

                if (declQualifier instanceof ConstantSlot declConstant) {
                    unannotatedDefault = declConstant;
                }
            }
            if (unannotatedDefault == null) {
                unannotatedDefault = slotManager.createConstantSlot(hierarchy, hierarchy.getTopQualifier());
            }
            return Pair.of(unannotatedDefault, false);
        }

        protected boolean shouldLookupTypeElementQualifiers(DeclaredType type) {
            return !ElementUtils.isLocalVariable(currentElement) && !ElementUtils.isBindingVariable(currentElement);
        }
    }
}
