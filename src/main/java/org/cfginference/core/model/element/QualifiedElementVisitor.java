package org.cfginference.core.model.element;

import org.cfginference.core.model.qualifier.Qualifier;

public interface QualifiedElementVisitor<Q extends Qualifier, R, P> {
    default R visit(QualifiedElement<Q> element) {
        return visit(element, null);
    }

    default R visit(QualifiedElement<Q> element, P p) {
        return element.accept(this, p);
    }

    R visitExecutable(QualifiedExecutableElement<Q> element, P p);

    R visitRecordComponent(QualifiedRecordComponentElement<Q> element, P p);

    R visitType(QualifiedTypeElement<Q> element, P p);

    R visitTypeParameter(QualifiedTypeParameterElement<Q> element, P p);

    R visitVariable(QualifiedVariableElement<Q> element, P p);
}
