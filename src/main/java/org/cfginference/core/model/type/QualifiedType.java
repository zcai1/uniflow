package org.cfginference.core.model.type;

import org.cfginference.core.model.qualifier.Qualifier;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Objects;

public abstract class QualifiedType<Q extends Qualifier> {
    @Nullable
    protected abstract Q getQualifier();

    public final Q getQualifierOrThrow() {
        Q qual = getQualifier();
        return Objects.requireNonNull(qual);
    }

    public abstract TypeMirror getJavaType();

    public final TypeKind getKind() {
        return getJavaType().getKind();
    }

    public abstract QualifiedType<Q> withQualifier(Q qualifier);

    public abstract <R, P> R accept(QualifiedTypeVisitor<Q, R, P> v, P p);
}
