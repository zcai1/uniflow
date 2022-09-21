package org.cfginference.core.model.type;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.cfginference.core.model.qualifier.Qualifier;

import javax.lang.model.type.IntersectionType;

@AutoValue
public abstract class QualifiedIntersectionType<Q extends Qualifier> extends PrimaryQualifiedType<Q> {
    @Override
    public abstract IntersectionType getJavaType();

    public abstract ImmutableList<QualifiedType<Q>> getBounds();

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedIntersectionType.Builder<>();
    }

    @Override
    public abstract Builder<Q> toBuilder();

    @Override
    public final QualifiedIntersectionType<Q> withQualifier(Q qualifier) {
        return toBuilder().setQualifier(qualifier).build();
    }

    @Override
    public final <R, P> R accept(QualifiedTypeVisitor<Q, R, P> v, P p) {
        return v.visitIntersection(this, p);
    }

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> extends PrimaryQualifiedType.Builder<Q> {
        @Override
        public abstract Builder<Q> setQualifier(Q qualifier);

        public abstract Builder<Q> setJavaType(IntersectionType type);

        public abstract ImmutableList.Builder<QualifiedType<Q>> boundsBuilder();

        public final Builder<Q> addBound(QualifiedType<Q> bound) {
            boundsBuilder().add(bound);
            return this;
        }

        public final Builder<Q> addBounds(Iterable<QualifiedType<Q>> bounds) {
            boundsBuilder().addAll(bounds);
            return this;
        }

        protected abstract QualifiedIntersectionType<Q> autoBuild();

        @Override
        public final QualifiedIntersectionType<Q> build() {
            QualifiedIntersectionType<Q> type = autoBuild();
            Preconditions.checkState(
                    type.getBounds().size() == type.getJavaType().getBounds().size(),
                    "Bounds size not matched"
            );
            return type;
        }
    }
}
