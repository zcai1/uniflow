package org.uniflow.core.model.type;

import com.google.auto.value.AutoValue;
import org.uniflow.core.model.qualifier.Qualifier;
import org.uniflow.core.model.util.QualifiedTypeVisitor;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.lang.model.type.WildcardType;

@AutoValue
public abstract class QualifiedWildcardType<Q extends Qualifier> extends PrimaryQualifiedType<Q> {
    @Override
    public abstract WildcardType getJavaType();

    // Returns the explicit upper bound if it exists; otherwise, returns
    // the upper bound of the corresponding type variable.
    public abstract QualifiedType<Q> getExtendsBound();

    // Returns the explicit lower bound if it exists; otherwise, returns
    // null.
    @Nullable
    public abstract QualifiedType<Q> getSuperBound();

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedWildcardType.Builder<>();
    }

    @Override
    public abstract Builder<Q> toBuilder();

    @Override
    public final QualifiedWildcardType<Q> withQualifier(Q qualifier) {
        return toBuilder().setQualifier(qualifier).build();
    }

    @Override
    public final <R, P> R accept(QualifiedTypeVisitor<Q, R, P> v, P p) {
        return v.visitWildcard(this, p);
    }

    @AutoValue.Builder
    public static abstract class Builder<Q extends Qualifier> extends PrimaryQualifiedType.Builder<Q> {
        @Override
        public abstract Builder<Q> setQualifier(Q qualifier);

        public abstract Builder<Q> setJavaType(WildcardType type);

        public abstract Builder<Q> setExtendsBound(QualifiedType<Q> extendsBound);

        public abstract Builder<Q> setSuperBound(@Nullable QualifiedType<Q> superBound);

        @Override
        public abstract QualifiedWildcardType<Q> build();
    }
}
