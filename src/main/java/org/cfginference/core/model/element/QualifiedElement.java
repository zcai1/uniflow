package org.cfginference.core.model.element;

import org.cfginference.core.model.qualifier.Qualifier;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.Objects;

public abstract class QualifiedElement<Q extends Qualifier> {

    @Nullable
    protected abstract Q getQualifier();

    public final Q getQualifierOrThrow() {
        Q qual = getQualifier();
        return Objects.requireNonNull(qual);
    }

    public abstract Element getJavaElement();

    public final ElementKind getKind() {
        return getJavaElement().getKind();
    }

    public abstract QualifiedElement<Q> withQualifier(Q qualifier);

    public abstract <R, P> R accept(QualifiedElementVisitor<Q, R, P> v, P p);
}
