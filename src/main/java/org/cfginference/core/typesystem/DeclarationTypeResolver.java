package org.cfginference.core.typesystem;

import org.cfginference.core.model.element.QualifiedElement;
import org.cfginference.core.model.element.QualifiedExecutableElement;
import org.cfginference.core.model.element.QualifiedRecordComponentElement;
import org.cfginference.core.model.element.QualifiedTypeElement;
import org.cfginference.core.model.element.QualifiedTypeParameterElement;
import org.cfginference.core.model.element.QualifiedVariableElement;
import org.cfginference.core.model.slot.ProductSlot;

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
