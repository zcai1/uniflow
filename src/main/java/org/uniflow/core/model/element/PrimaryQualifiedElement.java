package org.uniflow.core.model.element;

import org.uniflow.core.model.qualifier.Qualifier;

public abstract class PrimaryQualifiedElement<Q extends Qualifier> extends QualifiedElement<Q> {

    public abstract Q getQualifier();

    @Override
    public abstract Builder<Q> toBuilder();

    public abstract PrimaryQualifiedElement<Q> withQualifier(Q qualifier);

    public static abstract class Builder<Q extends Qualifier> extends QualifiedElement.Builder<Q> {
        public abstract Builder<Q> setQualifier(Q qualifier);

        @Override
        public PrimaryQualifiedElement<Q> build() {
            return null;
        }
    }
}
