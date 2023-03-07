package org.uniflow.core.model.util;

import org.uniflow.core.model.element.PrimaryQualifiedElement;
import org.uniflow.core.model.element.QualifiedTypeElement;
import org.uniflow.core.model.element.QualifiedTypeParameterElement;
import org.uniflow.core.model.qualifier.Qualifier;

public abstract class PrimaryQualifiedElementScanner<Q extends Qualifier, R, P>
        extends QualifiedElementScanner<Q, R, P> {

    protected abstract void visitPrimaryQualifiedElement(PrimaryQualifiedElement<Q> element, P p);

    @Override
    public R visitType(QualifiedTypeElement<Q> element, P p) {
        visitPrimaryQualifiedElement(element, p);
        return super.visitType(element, p);
    }

    @Override
    public R visitTypeParameter(QualifiedTypeParameterElement<Q> element, P p) {
        visitPrimaryQualifiedElement(element, p);
        return super.visitTypeParameter(element, p);
    }
}
