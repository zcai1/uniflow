package org.uniflow.core.model.util;

import org.uniflow.core.model.element.QualifiedExecutableElement;
import org.uniflow.core.model.element.QualifiedRecordComponentElement;
import org.uniflow.core.model.element.QualifiedTypeElement;
import org.uniflow.core.model.element.QualifiedTypeParameterElement;
import org.uniflow.core.model.element.QualifiedVariableElement;
import org.uniflow.core.model.qualifier.Qualifier;

public interface QualifiedElementVisitor<Q extends Qualifier, R, P> {
    R visitExecutable(QualifiedExecutableElement<Q> element, P p);

    R visitRecordComponent(QualifiedRecordComponentElement<Q> element, P p);

    R visitType(QualifiedTypeElement<Q> element, P p);

    R visitTypeParameter(QualifiedTypeParameterElement<Q> element, P p);

    R visitVariable(QualifiedVariableElement<Q> element, P p);
}
