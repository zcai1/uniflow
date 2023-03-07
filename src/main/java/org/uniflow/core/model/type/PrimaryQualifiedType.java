package org.uniflow.core.model.type;

import org.uniflow.core.model.qualifier.Qualifier;

public abstract class PrimaryQualifiedType<Q extends Qualifier> extends QualifiedType<Q> {

    public abstract Q getQualifier();

    @Override
    public abstract PrimaryQualifiedType.Builder<Q> toBuilder();

    public abstract PrimaryQualifiedType<Q> withQualifier(Q qualifier);

    public static abstract class Builder<Q extends Qualifier> extends QualifiedType.Builder<Q> {
        public abstract Builder<Q> setQualifier(Q qualifier);

        @Override
        public abstract PrimaryQualifiedType<Q> build();
    }
}
