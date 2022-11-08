package org.cfginference.core.model.type;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.util.QualifiedTypeVisitor;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public abstract class QualifiedType<Q extends Qualifier> {

    public abstract TypeMirror getJavaType();

    public final TypeKind getKind() {
        return getJavaType().getKind();
    }

    public abstract Builder<Q> toBuilder();

    public abstract <R, P> R accept(QualifiedTypeVisitor<Q, R, P> v, P p);

    public static abstract class Builder<Q extends Qualifier> {

        public abstract QualifiedType<Q> build();
    }
}
