package org.uniflow.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import java.util.Objects;

public class ElementHelpers {

    public enum AccessScope {
        SHARED, CLASS_LOCAL, METHOD_LOCAL
    }

    public static AccessScope getAccessScope(Element e) {
        switch (e.getKind()) {
            case EXCEPTION_PARAMETER, LOCAL_VARIABLE, RESOURCE_VARIABLE, BINDING_VARIABLE:
                return AccessScope.METHOD_LOCAL;
            case MODULE, PACKAGE:
                throw new IllegalArgumentException("Unexpected element " + e);
        }

        TypeElement enclosingType = null;
        ExecutableElement enclosingExecutable = null;
        Element curElement = e;
        while (curElement != null) {
            if (curElement instanceof ExecutableElement ee) {
                enclosingExecutable = ee;
            } else if (curElement instanceof TypeElement te) {
                enclosingType = te;
                // break when we've found the innermost enclosing type element
                break;
            }
            curElement = curElement.getEnclosingElement();
        }

        Objects.requireNonNull(enclosingType);

        if (enclosingType.getNestingKind() == NestingKind.ANONYMOUS
                || enclosingType.getNestingKind() == NestingKind.LOCAL) {
            return AccessScope.METHOD_LOCAL;
        }

        if (enclosingExecutable != null && e.getKind() == ElementKind.PARAMETER) {
            if (!enclosingExecutable.getParameters().contains(e)) {
                // this is the parameter of a lambda function
                return AccessScope.METHOD_LOCAL;
            }
        }

        if (e.getModifiers().contains(Modifier.PRIVATE)
                || enclosingType.getModifiers().contains(Modifier.PRIVATE)
                || (enclosingExecutable != null && enclosingExecutable.getModifiers().contains(Modifier.PRIVATE))) {
            return AccessScope.CLASS_LOCAL;
        }

        return AccessScope.SHARED;
    }

    public static boolean isEnclosing(@Nullable Element enclosingElement, @Nullable Element enclosedElement) {
        if (enclosingElement == null) {
            return false;
        }

        Element currentElement = enclosedElement;
        while (currentElement != null && !currentElement.equals(enclosingElement)) {
            currentElement = currentElement.getEnclosingElement();
        }
        return currentElement != null;
    }
}
