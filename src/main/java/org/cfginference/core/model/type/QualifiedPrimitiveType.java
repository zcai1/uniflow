package org.cfginference.core.model.type;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.qualifier.Qualifier;

import javax.lang.model.type.PrimitiveType;

@AutoValue
public abstract class QualifiedPrimitiveType<Q extends Qualifier> extends PrimaryQualifiedType<Q> {
    @Override
    public abstract PrimitiveType getJavaType();

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedPrimitiveType.Builder<>();
    }

    @Override
    public abstract Builder<Q> toBuilder();

    @Override
    public final QualifiedPrimitiveType<Q> withQualifier(Q qualifier) {
        return toBuilder().setQualifier(qualifier).build();
    }

    @Override
    public final <R, P> R accept(QualifiedTypeVisitor<Q, R, P> v, P p) {
        return v.visitPrimitive(this, p);
    }

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> extends PrimaryQualifiedType.Builder<Q> {
        @Override
        public abstract Builder<Q> setQualifier(Q qualifier);

        public abstract Builder<Q> setJavaType(PrimitiveType type);

        @Override
        public abstract QualifiedPrimitiveType<Q> build();
    }
}
