package org.uniflow.core.typesystem;

import org.uniflow.core.model.element.QualifiedElement;
import org.uniflow.core.model.element.QualifiedExecutableElement;
import org.uniflow.core.model.element.QualifiedRecordComponentElement;
import org.uniflow.core.model.element.QualifiedTypeElement;
import org.uniflow.core.model.element.QualifiedTypeParameterElement;
import org.uniflow.core.model.element.QualifiedVariableElement;
import org.uniflow.core.model.slot.ProductSlot;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;

public interface DeclarationTypeResolver {

    QualifiedElement<ProductSlot> getType(Element element);

    default QualifiedTypeElement<ProductSlot> getType(TypeElement element) {
        return (QualifiedTypeElement<ProductSlot>) getType((Element) element);
    }

    default QualifiedExecutableElement<ProductSlot> getType(ExecutableElement element) {
        return (QualifiedExecutableElement<ProductSlot>) getType((Element) element);
    }

    default QualifiedVariableElement<ProductSlot> getType(VariableElement element) {
        return (QualifiedVariableElement<ProductSlot>) getType((Element) element);
    }

    default QualifiedTypeParameterElement<ProductSlot> getType(TypeParameterElement element) {
        return (QualifiedTypeParameterElement<ProductSlot>) getType((Element) element);
    }

    default QualifiedRecordComponentElement<ProductSlot> getType(RecordComponentElement element) {
        return (QualifiedRecordComponentElement<ProductSlot>) getType((Element) element);
    }
}
