package org.cfginference.core.model.element;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.qualifier.Qualifier;

import javax.lang.model.element.VariableElement;

@AutoValue
public abstract class QualifiedVariableElement<Q extends Qualifier> extends QualifiedElement<Q> {
    @Override
    public abstract VariableElement getJavaElement();

    @Override
    public final QualifiedVariableElement<Q> withQualifier(Q qualifier) {
        return toBuilder().setQualifier(qualifier).build();
    }

    @Override
    public final <R, P> R accept(QualifiedElementVisitor<Q, R, P> v, P p) {
        return v.visitVariable(this, p);
    }

    public abstract Builder<Q> toBuilder();

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedVariableElement.Builder<>();
    }

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> {
        public abstract Builder<Q> setQualifier(Q qualifier);
        public abstract Builder<Q> setJavaElement(VariableElement element);
        public abstract QualifiedVariableElement<Q> build();
    }
}
