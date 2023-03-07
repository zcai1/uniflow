package org.uniflow.core.model.element;

import com.google.auto.value.AutoValue;
import org.uniflow.core.model.qualifier.Qualifier;
import org.uniflow.core.model.type.QualifiedType;
import org.uniflow.core.model.util.QualifiedElementVisitor;

import javax.lang.model.element.VariableElement;

@AutoValue
public abstract class QualifiedVariableElement<Q extends Qualifier> extends QualifiedElement<Q> {
    @Override
    public abstract VariableElement getJavaElement();

    public abstract QualifiedType<Q> getType();

    @Override
    public final <R, P> R accept(QualifiedElementVisitor<Q, R, P> v, P p) {
        return v.visitVariable(this, p);
    }

    @Override
    public abstract Builder<Q> toBuilder();

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedVariableElement.Builder<>();
    }

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> extends QualifiedElement.Builder<Q> {
        public abstract Builder<Q> setJavaElement(VariableElement element);

        public abstract Builder<Q> setType(QualifiedType<Q> type);

        @Override
        public abstract QualifiedVariableElement<Q> build();
    }
}
