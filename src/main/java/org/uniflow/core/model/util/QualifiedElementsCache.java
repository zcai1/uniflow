package org.uniflow.core.model.util;

import org.uniflow.core.model.element.QualifiedElement;
import org.uniflow.core.model.reporting.PluginError;
import org.uniflow.core.model.qualifier.Qualifier;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.javacutil.ElementUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.IdentityHashMap;
import java.util.Objects;

public class QualifiedElementsCache<Q extends Qualifier> {

    // Elements that can possibly be accessed in classes other than the current
    // top-level class.
    private final IdentityHashMap<Element, QualifiedElement<Q>> globalElements;

    // Elements that can only be accessed in the current top-level class, which
    // can be safely discarded once we've finished analyzing the class.
    private final IdentityHashMap<Element, QualifiedElement<Q>> localElements;

    public QualifiedElementsCache() {
        this.globalElements = new IdentityHashMap<>();
        this.localElements = new IdentityHashMap<>();
    }

    public void clearLocalElements() {
        this.localElements.clear();
    }

    public void save(@Nullable QualifiedElement<Q> value) {
        if (value != null) {
            Element element = value.getJavaElement();
            if (isGlobalElement(element)) {
                globalElements.put(element, value);
            } else {
                localElements.put(element, value);
            }
        }
    }

    public @Nullable QualifiedElement<Q> load(Element key) {
        QualifiedElement<Q> value = localElements.get(key);
        if (value != null) {
            return value;
        }

        return globalElements.get(key);
    }

    // public QualifiedTypeElement<Q> load(TypeElement key) {
    //     return (QualifiedTypeElement<Q>) load((Element) key);
    // }
    //
    // public QualifiedExecutableElement<Q> load(ExecutableElement key) {
    //     return (QualifiedExecutableElement<Q>) load((Element) key);
    // }
    //
    // public QualifiedVariableElement<Q> load(VariableElement key) {
    //     return (QualifiedVariableElement<Q>) load((Element) key);
    // }
    //
    // public QualifiedTypeParameterElement<Q> load(TypeParameterElement key) {
    //     return (QualifiedTypeParameterElement<Q>) load((Element) key);
    // }
    //
    // public QualifiedRecordComponentElement<Q> load(RecordComponentElement key) {
    //     return (QualifiedRecordComponentElement<Q>) load((Element) key);
    // }

    private boolean isGlobalElement(Element element) {
        switch (element.getKind()) {
            // Local variables
            case EXCEPTION_PARAMETER, LOCAL_VARIABLE, RESOURCE_VARIABLE, BINDING_VARIABLE -> {
                return false;
            }
            // Declared types
            case ENUM, CLASS, RECORD, INTERFACE, ANNOTATION_TYPE -> {
                TypeElement typeElement = (TypeElement) element;
                NestingKind nestingKind = typeElement.getNestingKind();

                // Treat private inner class as a global element because a public class could be
                // its subtype.
                return nestingKind != NestingKind.ANONYMOUS && nestingKind != NestingKind.LOCAL;
            }
            // Direct class members
            case ENUM_CONSTANT, FIELD, CONSTRUCTOR, METHOD, RECORD_COMPONENT -> {
                if (element.getModifiers().contains(Modifier.PRIVATE)) {
                    return false;
                }

                TypeElement enclosingTypeElement = Objects.requireNonNull(ElementUtils.enclosingTypeElement(element));
                return isGlobalElement(enclosingTypeElement);
            }
            // Others
            case PARAMETER -> {
                ExecutableElement enclosingExecutable = (ExecutableElement) element.getEnclosingElement();
                TypeMirror receiverType = enclosingExecutable.getReceiverType();
                Element receiverElement = null;

                if (receiverType.getKind() == TypeKind.DECLARED) {
                    receiverElement = ((DeclaredType) receiverType).asElement();
                }
                if (!element.equals(receiverElement) && !enclosingExecutable.getParameters().contains(element)) {
                    // parameter of a lambda expression
                    return false;
                }
                return isGlobalElement(enclosingExecutable);
            }
            case TYPE_PARAMETER -> {
                return isGlobalElement(element.getEnclosingElement());
            }
            default -> {
                throw new PluginError("Unexpected element: %s", element.getKind());
            }
        }
    }
}
