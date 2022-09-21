package org.cfginference.core.model.type;

import com.google.auto.value.AutoValue;
import org.cfginference.core.model.qualifier.Qualifier;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.lang.model.type.NoType;

@AutoValue
public abstract class QualifiedNoType<Q extends Qualifier> extends QualifiedType<Q> {
    @Override
    public abstract NoType getJavaType();

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedNoType.Builder<>();
    }

    @Override
    public abstract Builder<Q> toBuilder();

    @Override
    public final <R, P> R accept(QualifiedTypeVisitor<Q, R, P> v, P p) {
        return v.visitNo(this, p);
    }

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> extends QualifiedType.Builder<Q> {
        public abstract Builder<Q> setJavaType(NoType type);

        @Override
        public abstract QualifiedNoType<Q> build();
    }
}
