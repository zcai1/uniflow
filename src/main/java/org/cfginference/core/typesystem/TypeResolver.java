package org.cfginference.core.typesystem;

import org.cfginference.core.model.element.QualifiedElement;
import org.cfginference.core.model.slot.ProductSlot;
import org.checkerframework.javacutil.TypesUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

public interface TypeResolver {
    QualifiedElement<ProductSlot> getDeclaredType(Element element);

    boolean isSideEffectFree(ExecutableElement element);

    default boolean isImmutable(TypeMirror type) {
        return TypesUtils.isImmutableTypeInJdk(type);
    }
}
