package org.cfginference.core.model.type;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.qualifier.Qualifier;

import javax.lang.model.type.TypeVariable;

@AutoValue
public abstract class QualifiedTypeVariable<Q extends Qualifier> extends PrimaryQualifiedType<Q> {
    @Override
    public abstract TypeVariable getJavaType();

    public abstract QualifiedType<Q> getUpperBound();

    public abstract QualifiedType<Q> getLowerBound();

    public static <Q extends Qualifier> Builder<Q> builder() {
     return new AutoValue_QualifiedTypeVariable.Builder<>();
    }

    @Override
    public abstract Builder<Q> toBuilder();

    @Override
    public final QualifiedTypeVariable<Q> withQualifier(Q qualifier) {
        return toBuilder().setQualifier(qualifier).build();
    }

    @Override
    public final <R, P> R accept(QualifiedTypeVisitor<Q, R, P> v, P p) {
        return v.visitTypeVariable(this, p);
    }

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> extends PrimaryQualifiedType.Builder<Q> {
        @Override
        public abstract Builder<Q> setQualifier(Q qualifier);

        public abstract Builder<Q> setJavaType(TypeVariable type);

        public abstract Builder<Q> setUpperBound(QualifiedType<Q> upperBound);

        public abstract Builder<Q> setLowerBound(QualifiedType<Q> lowerBound);

        @Override
        public abstract QualifiedTypeVariable<Q> build();
    }
}
