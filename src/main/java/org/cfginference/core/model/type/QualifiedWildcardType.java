package org.cfginference.core.model.type;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.qualifier.Qualifier;

import javax.lang.model.type.WildcardType;

@AutoValue
public abstract class QualifiedWildcardType<Q extends Qualifier> extends QualifiedType<Q> {
    @Override
    public abstract WildcardType getJavaType();

    public abstract QualifiedType<Q> getExtendsBound();

    public abstract QualifiedType<Q> getSuperBound();

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedWildcardType.Builder<>();
    }

    public abstract Builder<Q> toBuilder();

    @Override
    public QualifiedWildcardType<Q> withQualifier(Q qualifier) {
        return toBuilder().setQualifier(qualifier).build();
    }

    @Override
    public final <R, P> R accept(QualifiedTypeVisitor<Q, R, P> v, P p) {
        return v.visitWildcard(this, p);
    }

    @AutoValue.Builder
    public static abstract class Builder<Q extends Qualifier> {
        public abstract Builder<Q> setQualifier(Q qualifier);
        public abstract Builder<Q> setJavaType(WildcardType type);
        public abstract Builder<Q> setExtendsBound(QualifiedType<Q> extendsBound);
        public abstract Builder<Q> setSuperBound(QualifiedType<Q> superBound);
        public abstract QualifiedWildcardType<Q> build();
    }
}
