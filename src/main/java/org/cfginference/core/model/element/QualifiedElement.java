package org.cfginference.core.model.element;

import org.cfginference.core.model.qualifier.Qualifier;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

public abstract class QualifiedElement<Q extends Qualifier> {

    public Q getQualifier() {
        throw new UnsupportedOperationException("No primary qualifier for " + this.getClass().getSimpleName());
    }

    public final boolean hasQualifier() {
        return false;
    }

    public abstract Element getJavaElement();

    public final ElementKind getKind() {
        return getJavaElement().getKind();
    }

    public abstract Builder<Q> toBuilder();

    public abstract <R, P> R accept(QualifiedElementVisitor<Q, R, P> v, P p);

    public static abstract class Builder<Q extends Qualifier> {
        public abstract QualifiedElement<Q> build();
    }
}
