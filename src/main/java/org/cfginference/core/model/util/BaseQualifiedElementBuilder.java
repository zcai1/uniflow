package org.cfginference.core.model.util;

import org.cfginference.core.model.element.QualifiedElement;
import org.cfginference.core.model.element.QualifiedExecutableElement;
import org.cfginference.core.model.element.QualifiedRecordComponentElement;
import org.cfginference.core.model.element.QualifiedTypeElement;
import org.cfginference.core.model.element.QualifiedTypeParameterElement;
import org.cfginference.core.model.element.QualifiedVariableElement;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.util.BaseQualifiedTypeBuilder;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor14;

public class BaseQualifiedElementBuilder<Q extends Qualifier, P> extends AbstractElementVisitor14<QualifiedElement<Q>, P> {

    protected final BaseQualifiedTypeBuilder<Q, P> typeBuilder;

    public BaseQualifiedElementBuilder(BaseQualifiedTypeBuilder<Q, P> typeBuilder) {
        this.typeBuilder = typeBuilder;
    }

    @Override
    public QualifiedRecordComponentElement<Q> visitRecordComponent(RecordComponentElement e, P p) {
        return defaultBuilder(e, p).build();
    }

    protected QualifiedRecordComponentElement.Builder<Q> defaultBuilder(RecordComponentElement e, P p) {
        return QualifiedRecordComponentElement.<Q>builder()
                .setJavaElement(e)
                .setAccessor(visitExecutable(e.getAccessor(), p))
                .setType(typeBuilder.visit(e.asType(), p));
    }

    @Override
    public QualifiedTypeElement<Q> visitType(TypeElement e, P p) {
        return defaultBuilder(e, p).build();
    }

    protected QualifiedTypeElement.Builder<Q> defaultBuilder(TypeElement e, P p) {
        return QualifiedTypeElement.<Q>builder()
                .setJavaElement(e)
                .setSuperClass(typeBuilder.visit(e.getSuperclass(), p))
                .setInterfaces(e.getInterfaces().stream().map(i -> typeBuilder.visit(i, p)).toList())
                .setTypeParameters(e.getTypeParameters().stream().map(tp -> visitTypeParameter(tp, p)).toList())
                .setRecordComponents(e.getRecordComponents().stream().map(rc -> visitRecordComponent(rc, p)).toList());
    }

    @Override
    public QualifiedVariableElement<Q> visitVariable(VariableElement e, P p) {
        return defaultBuilder(e, p).build();
    }

    protected QualifiedVariableElement.Builder<Q> defaultBuilder(VariableElement e, P p) {
        return QualifiedVariableElement.<Q>builder()
                .setJavaElement(e)
                .setType(typeBuilder.visit(e.asType(), p));
    }

    @Override
    public QualifiedExecutableElement<Q> visitExecutable(ExecutableElement e, P p) {
        return defaultBuilder(e, p).build();
    }

    protected QualifiedExecutableElement.Builder<Q> defaultBuilder(ExecutableElement e, P p) {
        return QualifiedExecutableElement.<Q>builder()
                .setJavaElement(e)
                .setTypeParameters(e.getTypeParameters().stream().map(tp -> visitTypeParameter(tp, p)).toList())
                .setReturnType(typeBuilder.visit(e.getReturnType(), p))
                .setParameters(e.getParameters().stream().map(param -> visitVariable(param, p)).toList())
                .setReceiverType(typeBuilder.visit(e.getReceiverType(), p))
                .setThrownTypes(e.getThrownTypes().stream().map(t -> typeBuilder.visit(t, p)).toList());
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
    public QualifiedTypeParameterElement<Q> visitTypeParameter(TypeParameterElement e, P p) {
        // TODO(generics): implementation
        throw new UnsupportedOperationException("Qualified %s is not supported".formatted(e));
    }
}
