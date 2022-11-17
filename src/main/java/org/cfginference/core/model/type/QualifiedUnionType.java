package org.cfginference.core.model.type;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.util.QualifiedTypeVisitor;

import javax.lang.model.type.UnionType;

@AutoValue
public abstract class QualifiedUnionType<Q extends Qualifier> extends PrimaryQualifiedType<Q> {
    @Override
    public abstract UnionType getJavaType();

    public abstract ImmutableList<QualifiedType<Q>> getAlternatives();

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedUnionType.Builder<>();
    }

    @Override
    public abstract Builder<Q> toBuilder();

    @Override
    public final QualifiedUnionType<Q> withQualifier(Q qualifier) {
        return toBuilder().setQualifier(qualifier).build();
    }

    @Override
    public final <R, P> R accept(QualifiedTypeVisitor<Q, R, P> v, P p) {
        return v.visitUnion(this, p);
    }

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> extends PrimaryQualifiedType.Builder<Q> {
        @Override
        public abstract Builder<Q> setQualifier(Q qualifier);

        public abstract Builder<Q> setJavaType(UnionType type);

        public abstract ImmutableList.Builder<QualifiedType<Q>> alternativesBuilder();

        public abstract Builder<Q> setAlternatives(Iterable<QualifiedType<Q>> alternatives);

        public final Builder<Q> addAlternative(QualifiedType<Q> alternative) {
            alternativesBuilder().add(alternative);
            return this;
        }

        public final Builder<Q> addAlternatives(Iterable<QualifiedType<Q>> alternatives) {
            alternativesBuilder().addAll(alternatives);
            return this;
        }

        protected abstract QualifiedUnionType<Q> autoBuild();

        @Override
        public final QualifiedUnionType<Q> build() {
            QualifiedUnionType<Q> type = autoBuild();
            Preconditions.checkState(
                    type.getAlternatives().size() == type.getJavaType().getAlternatives().size(),
                    "Alternatives size not matched"
            );
            return type;
        }
    }
}
