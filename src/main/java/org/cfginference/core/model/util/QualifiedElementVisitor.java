package org.cfginference.core.model.util;

import org.cfginference.core.model.element.QualifiedExecutableElement;
import org.cfginference.core.model.element.QualifiedRecordComponentElement;
import org.cfginference.core.model.element.QualifiedTypeElement;
import org.cfginference.core.model.element.QualifiedTypeParameterElement;
import org.cfginference.core.model.element.QualifiedVariableElement;
import org.cfginference.core.model.qualifier.Qualifier;

public interface QualifiedElementVisitor<Q extends Qualifier, R, P> {
    R visitExecutable(QualifiedExecutableElement<Q> element, P p);

    R visitRecordComponent(QualifiedRecordComponentElement<Q> element, P p);

    R visitType(QualifiedTypeElement<Q> element, P p);

    R visitTypeParameter(QualifiedTypeParameterElement<Q> element, P p);

    R visitVariable(QualifiedVariableElement<Q> element, P p);
}
