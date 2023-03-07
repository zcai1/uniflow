package org.uniflow.core.model.util;

import org.uniflow.core.model.element.QualifiedElement;
import org.uniflow.core.model.element.QualifiedExecutableElement;
import org.uniflow.core.model.element.QualifiedRecordComponentElement;
import org.uniflow.core.model.element.QualifiedTypeElement;
import org.uniflow.core.model.element.QualifiedTypeParameterElement;
import org.uniflow.core.model.element.QualifiedVariableElement;
import org.uniflow.core.model.qualifier.Qualifier;

public class SimpleQualifiedElementVisitor<Q extends Qualifier, R, P> implements QualifiedElementVisitor<Q, R, P> {

    protected final R DEFAULT_VALUE;

    protected SimpleQualifiedElementVisitor() {
        DEFAULT_VALUE = null;
    }

    protected SimpleQualifiedElementVisitor(R defaultValue) {
        DEFAULT_VALUE = defaultValue;
    }

    protected R defaultAction(QualifiedElement<Q> element, P p) {
        return DEFAULT_VALUE;
    }

    public R visit(QualifiedElement<Q> element, P p) {
        return (element == null) ? null : element.accept(this, p);
    }

    @Override
    public R visitExecutable(QualifiedExecutableElement<Q> element, P p) {
        return defaultAction(element, null);
    }

    @Override
    public R visitRecordComponent(QualifiedRecordComponentElement<Q> element, P p) {
        return defaultAction(element, null);
    }

    @Override
    public R visitType(QualifiedTypeElement<Q> element, P p) {
        return defaultAction(element, null);
    }

    @Override
    public R visitTypeParameter(QualifiedTypeParameterElement<Q> element, P p) {
        return defaultAction(element, null);
    }

    @Override
    public R visitVariable(QualifiedVariableElement<Q> element, P p) {
        return defaultAction(element, null);
    }
}
