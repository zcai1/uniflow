package org.cfginference.core.model.util;

import org.cfginference.core.model.element.QualifiedElement;
import org.cfginference.core.model.element.QualifiedExecutableElement;
import org.cfginference.core.model.element.QualifiedRecordComponentElement;
import org.cfginference.core.model.element.QualifiedTypeElement;
import org.cfginference.core.model.element.QualifiedTypeParameterElement;
import org.cfginference.core.model.element.QualifiedVariableElement;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.type.QualifiedType;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

public abstract class QualifiedElementBuilder<Q extends Qualifier, P>
        implements ElementVisitor<QualifiedElement<Q>, P> {

    protected abstract Q getQualifier(Element e, P p);

    protected abstract QualifiedType<Q> getQualifiedType(TypeMirror t, Element e, P p);

    @Override
    public QualifiedElement<Q> visit(Element e, P p) {
        return e.accept(this, p);
    }

    @Override
    public QualifiedRecordComponentElement<Q> visitRecordComponent(RecordComponentElement e, P p) {
        return defaultBuilder(e, p).build();
    }

    protected QualifiedRecordComponentElement.Builder<Q> defaultBuilder(RecordComponentElement e, P p) {
        return QualifiedRecordComponentElement.<Q>builder()
                .setJavaElement(e)
                .setAccessor((QualifiedExecutableElement<Q>) visit(e.getAccessor(), p))
                .setType(getQualifiedType(e.asType(), e, p));
    }

    @Override
    public QualifiedTypeElement<Q> visitType(TypeElement e, P p) {
        return defaultBuilder(e, p).build();
    }

    protected QualifiedTypeElement.Builder<Q> defaultBuilder(TypeElement e, P p) {
        List<QualifiedTypeParameterElement<Q>> typeParams = e.getTypeParameters().stream()
                .map(tp -> (QualifiedTypeParameterElement<Q>) visit(tp, p))
                .toList();
        List<QualifiedRecordComponentElement<Q>> recordComponents = e.getRecordComponents().stream()
                .map(rc -> (QualifiedRecordComponentElement<Q>) visit(rc, p))
                .toList();

        return QualifiedTypeElement.<Q>builder()
                .setJavaElement(e)
                .setSuperClass(getQualifiedType(e.getSuperclass(), e, p))
                .setInterfaces(e.getInterfaces().stream().map(i -> getQualifiedType(i, e, p)).toList())
                .setTypeParameters(typeParams)
                .setRecordComponents(recordComponents)
                .setQualifier(getQualifier(e, p));
    }

    @Override
    public QualifiedVariableElement<Q> visitVariable(VariableElement e, P p) {
        return defaultBuilder(e, p).build();
    }

    protected QualifiedVariableElement.Builder<Q> defaultBuilder(VariableElement e, P p) {
        return QualifiedVariableElement.<Q>builder()
                .setJavaElement(e)
                .setType(getQualifiedType(e.asType(), e, p));
    }

    @Override
    public QualifiedExecutableElement<Q> visitExecutable(ExecutableElement e, P p) {
        return defaultBuilder(e, p).build();
    }

    protected QualifiedExecutableElement.Builder<Q> defaultBuilder(ExecutableElement e, P p) {
        List<QualifiedTypeParameterElement<Q>> typeParams = e.getTypeParameters().stream()
                .map(tp -> (QualifiedTypeParameterElement<Q>) visit(tp, p))
                .toList();
        List<QualifiedVariableElement<Q>> params = e.getParameters().stream()
                .map(param -> (QualifiedVariableElement<Q>) visit(param, p))
                .toList();

        return QualifiedExecutableElement.<Q>builder()
                .setJavaElement(e)
                .setTypeParameters(typeParams)
                .setReturnType(getQualifiedType(e.getReturnType(), e, p))
                .setParameters(params)
                .setReceiverType(getQualifiedType(e.getReceiverType(), e, p))
                .setThrownTypes(e.getThrownTypes().stream().map(t -> getQualifiedType(t, e, p)).toList());
    }

    @Override
    public QualifiedElement<Q> visitModule(ModuleElement e, P p) {
        throw new UnsupportedOperationException("Qualified %s is not supported".formatted(e));
    }

    @Override
    public QualifiedElement<Q> visitPackage(PackageElement e, P p) {
        throw new UnsupportedOperationException("Qualified %s is not supported".formatted(e));
    }

    @Override
    public QualifiedElement<Q> visitUnknown(Element e, P p) {
        throw new UnsupportedOperationException("Qualified %s is not supported".formatted(e));
    }

    @Override
    public QualifiedTypeParameterElement<Q> visitTypeParameter(TypeParameterElement e, P p) {
        // TODO(generics): implementation
        throw new UnsupportedOperationException("Qualified %s is not supported".formatted(e));
    }
}
