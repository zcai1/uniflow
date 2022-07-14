package org.cfginference.core.manager;

import org.cfginference.core.model.element.QualifiedElement;
import org.cfginference.core.model.element.QualifiedExecutableElement;
import org.cfginference.core.model.element.QualifiedTypeElement;
import org.cfginference.core.model.element.QualifiedTypeParameterElement;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.type.QualifiedType;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import java.util.List;

public class QualifiedElementFactory {
    public static <Q extends Qualifier> QualifiedElement<Q> createRaw(Element element) {
        switch (element.getKind()) {
            case ENUM:
            case CLASS:
            case ANNOTATION_TYPE:
            case INTERFACE:
            case RECORD:
                return createRaw((TypeElement) element);
//            case ENUM_CONSTANT -> null;
//            case FIELD -> null;
//            case PARAMETER -> null;
//            case LOCAL_VARIABLE -> null;
//            case EXCEPTION_PARAMETER -> null;
//            case METHOD -> null;
//            case CONSTRUCTOR -> null;
//            case STATIC_INIT -> null;
//            case INSTANCE_INIT -> null;
//            case TYPE_PARAMETER -> null;
//            case OTHER -> null;
//            case RESOURCE_VARIABLE -> null;
//            case MODULE -> null;
//
//            case RECORD_COMPONENT -> null;
//            case BINDING_VARIABLE -> null;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static <Q extends Qualifier> QualifiedTypeElement<Q> createRaw(TypeElement element) {
        List<QualifiedType<Q>> interfaces = element.getInterfaces().stream()
                .map(QualifiedTypeFactory::<Q>createRaw).toList();
        List<QualifiedTypeParameterElement<Q>> typeParams = element.getTypeParameters().stream()
                .map(QualifiedElementFactory::<Q>createRaw).toList();
        return QualifiedTypeElement.<Q>builder()
                .setJavaElement(element)
                .setSuperClass(QualifiedTypeFactory.createRaw(element.getSuperclass()))
                .addInterfaces(interfaces)
                .addTypeParameters(typeParams)
                .build();
    }

    public static <Q extends Qualifier> QualifiedTypeParameterElement<Q> createRaw(TypeParameterElement element) {
        List<QualifiedType<Q>> bounds = element.getBounds().stream()
                .map(QualifiedTypeFactory::<Q>createRaw).toList();
        return QualifiedTypeParameterElement.<Q>builder()
                .setJavaElement(element)
                .addBounds(bounds)
                .build();
    }

    public static <Q extends Qualifier> QualifiedExecutableElement<Q> createRaw(ExecutableElement element) {

    }
}
