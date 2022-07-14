package org.cfginference.core.model.type;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.qualifier.Qualifier;

import javax.lang.model.type.NoType;

@AutoValue
public abstract class QualifiedNoType<Q extends Qualifier> extends QualifiedType<Q> {
    @Override
    public abstract NoType getJavaType();

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedNoType.Builder<>();
    }

    public abstract Builder<Q> toBuilder();

    @Override
    public QualifiedNoType<Q> withQualifier(Q qualifier) {
        return toBuilder().setQualifier(qualifier).build();
    }

    @Override
    public final <R, P> R accept(QualifiedTypeVisitor<Q, R, P> v, P p) {
        return v.visitNo(this, p);
    }

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> {
        public abstract Builder<Q> setQualifier(Q qualifier);
        public abstract Builder<Q> setJavaType(NoType type);
        public abstract QualifiedNoType<Q> build();
    }
}
