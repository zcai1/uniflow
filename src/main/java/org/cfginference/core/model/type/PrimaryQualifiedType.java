package org.cfginference.core.model.type;

import org.cfginference.core.model.qualifier.Qualifier;

public abstract class PrimaryQualifiedType<Q extends Qualifier> extends QualifiedType<Q> {

    @Override
    public abstract Q getQualifier();

    @Override
    public final boolean hasQualifier() {
        return true;
    }

    @Override
    public abstract PrimaryQualifiedType.Builder<Q> toBuilder();

    public abstract PrimaryQualifiedType<Q> withQualifier(Q qualifier);

    public static abstract class Builder<Q extends Qualifier> extends QualifiedType.Builder<Q> {
        public abstract Builder<Q> setQualifier(Q qualifier);

        @Override
        public abstract PrimaryQualifiedType<Q> build();
    }
}
