package org.uniflow.core.model.type;

import com.google.auto.value.AutoValue;
import org.uniflow.core.model.qualifier.Qualifier;
import org.uniflow.core.model.util.QualifiedTypeVisitor;

import javax.lang.model.type.ArrayType;

@AutoValue
public abstract class QualifiedArrayType<Q extends Qualifier> extends PrimaryQualifiedType<Q> {

    @Override
    public abstract ArrayType getJavaType();

    public abstract QualifiedType<Q> getComponentType();

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedArrayType.Builder<>();
    }

    @Override
    public abstract Builder<Q> toBuilder();

    @Override
    public final QualifiedArrayType<Q> withQualifier(Q qualifier) {
        return toBuilder().setQualifier(qualifier).build();
    }

    @Override
    public final <R, P> R accept(QualifiedTypeVisitor<Q, R, P> v, P p) {
        return v.visitArray(this, p);
    }

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> extends PrimaryQualifiedType.Builder<Q> {
        @Override
        public abstract Builder<Q> setQualifier(Q qualifier);

        public abstract Builder<Q> setJavaType(ArrayType type);

        public abstract Builder<Q> setComponentType(QualifiedType<Q> componentType);

        @Override
        public abstract QualifiedArrayType<Q> build();
    }
}
