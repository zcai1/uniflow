package org.uniflow.core.model.element;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.uniflow.core.model.qualifier.Qualifier;
import org.uniflow.core.model.type.QualifiedType;
import org.uniflow.core.model.util.QualifiedElementVisitor;

import javax.lang.model.element.TypeParameterElement;

@AutoValue
public abstract class QualifiedTypeParameterElement<Q extends Qualifier> extends PrimaryQualifiedElement<Q> {
    @Override
    public abstract TypeParameterElement getJavaElement();

    public abstract ImmutableList<QualifiedType<Q>> getBounds();

    @Override
    public QualifiedTypeParameterElement<Q> withQualifier(Q qualifier) {
        return toBuilder().setQualifier(qualifier).build();
    }

    @Override
    public final <R, P> R accept(QualifiedElementVisitor<Q, R, P> v, P p) {
        return v.visitTypeParameter(this, p);
    }

    @Override
    public abstract Builder<Q> toBuilder();

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedTypeParameterElement.Builder<>();
    }

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> extends PrimaryQualifiedElement.Builder<Q> {
        @Override
        public abstract Builder<Q> setQualifier(Q qualifier);

        public abstract Builder<Q> setJavaElement(TypeParameterElement element);

        public abstract ImmutableList.Builder<QualifiedType<Q>> boundsBuilder();

        public abstract Builder<Q> setBounds(Iterable<QualifiedType<Q>> bounds);

        public final Builder<Q> addBound(QualifiedType<Q> bound) {
            boundsBuilder().add(bound);
            return this;
        }

        public final Builder<Q> addBounds(Iterable<QualifiedType<Q>> bounds) {
            boundsBuilder().addAll(bounds);
            return this;
        }

        protected abstract QualifiedTypeParameterElement<Q> autoBuild();

        @Override
        public final QualifiedTypeParameterElement<Q> build() {
            QualifiedTypeParameterElement<Q> element = autoBuild();
            Preconditions.checkState(
                    element.getBounds().size() == element.getJavaElement().getBounds().size(),
                    "Bounds not matched"
            );
            return element;
        }
    }
}
