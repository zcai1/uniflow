package org.cfginference.core.model.type;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.cfginference.core.model.qualifier.Qualifier;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.lang.model.type.ExecutableType;

@AutoValue
public abstract class QualifiedExecutableType<Q extends Qualifier> extends QualifiedType<Q> {

    @Override
    public abstract ExecutableType getJavaType();

    public abstract ImmutableList<QualifiedTypeVariable<Q>> getTypeVariables();

    public abstract QualifiedType<Q> getReturnType();

    public abstract ImmutableList<QualifiedType<Q>> getParameterTypes();

    public abstract QualifiedType<Q> getReceiverType();

    public abstract ImmutableList<QualifiedType<Q>> getThrownTypes();

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedExecutableType.Builder<>();
    }

    @Override
    public abstract Builder<Q> toBuilder();

    @Override
    public final <R, P> R accept(QualifiedTypeVisitor<Q, R, P> v, P p) {
        return v.visitExecutable(this, p);
    }

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> extends QualifiedType.Builder<Q> {

        public abstract Builder<Q> setJavaType(ExecutableType type);

        public abstract Builder<Q> setReturnType(QualifiedType<Q> returnType);

        public abstract Builder<Q> setReceiverType(QualifiedType<Q> receiverType);

        public abstract ImmutableList.Builder<QualifiedTypeVariable<Q>> typeVariablesBuilder();

        public final Builder<Q> addTypeVariable(QualifiedTypeVariable<Q> typeVariable) {
            typeVariablesBuilder().add(typeVariable);
            return this;
        }

        public final Builder<Q> addTypeVariables(Iterable<QualifiedTypeVariable<Q>> typeVariables) {
            typeVariablesBuilder().addAll(typeVariables);
            return this;
        }

        public abstract ImmutableList.Builder<QualifiedType<Q>> parameterTypesBuilder();

        public final Builder<Q> addParameterType(QualifiedType<Q> parameterType) {
            parameterTypesBuilder().add(parameterType);
            return this;
        }

        public final Builder<Q> addParameterTypes(Iterable<QualifiedType<Q>> parameterTypes) {
            parameterTypesBuilder().addAll(parameterTypes);
            return this;
        }

        public abstract ImmutableList.Builder<QualifiedType<Q>> thrownTypesBuilder();

        public final Builder<Q> addThrownType(QualifiedType<Q> thrownType) {
            thrownTypesBuilder().add(thrownType);
            return this;
        }

        public final Builder<Q> addThrownTypes(Iterable<QualifiedType<Q>> thrownTypes) {
            thrownTypesBuilder().addAll(thrownTypes);
            return this;
        }

        protected abstract QualifiedExecutableType<Q> autoBuild();

        @Override
        public final QualifiedExecutableType<Q> build() {
            QualifiedExecutableType<Q> type = autoBuild();

            Preconditions.checkState(
                    type.getParameterTypes().size() == type.getJavaType().getParameterTypes().size(),
                    "Parameter types size not matched"
            );
            Preconditions.checkState(
                    type.getThrownTypes().size() == type.getJavaType().getThrownTypes().size(),
                    "Thrown types size not matched"
            );
            Preconditions.checkState(
                    type.getTypeVariables().size() == type.getJavaType().getTypeVariables().size(),
                    "Type variables size not matched"
            );

            return type;
        }
    }
}
