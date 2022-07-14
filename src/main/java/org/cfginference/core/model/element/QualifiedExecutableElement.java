package org.cfginference.core.model.element;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.cfginference.core.model.type.QualifiedType;
import org.cfginference.core.model.qualifier.Qualifier;

import javax.lang.model.element.ExecutableElement;

@AutoValue
public abstract class QualifiedExecutableElement<Q extends Qualifier> extends QualifiedElement<Q> {

    @Override
    public abstract ExecutableElement getJavaElement();

    public abstract ImmutableList<QualifiedTypeParameterElement<Q>> getTypeParameters();

    public abstract ImmutableList<QualifiedVariableElement<Q>> getParameters();

    public abstract ImmutableList<QualifiedType<Q>> getThrownTypes();

    public abstract QualifiedType<Q> getReturnType();

    public abstract QualifiedType<Q> getReceiverType();

    @Override
    public final QualifiedExecutableElement<Q> withQualifier(Q qualifier) {
        throw new UnsupportedOperationException("No primary qualifier for executable element");
    }

    @Override
    public final <R, P> R accept(QualifiedElementVisitor<Q, R, P> v, P p) {
        return v.visitExecutable(this, p);
    }

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedExecutableElement.Builder<>();
    }

    public abstract Builder<Q> toBuilder();

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> {
        public abstract Builder<Q> setQualifier(Q qualifier);
        public abstract Builder<Q> setJavaElement(ExecutableElement element);
        public abstract Builder<Q> setReturnType(QualifiedType<Q> type);
        public abstract Builder<Q> setReceiverType(QualifiedType<Q> type);
        public abstract ImmutableList.Builder<QualifiedTypeParameterElement<Q>> typeParametersBuilder();
        public abstract ImmutableList.Builder<QualifiedVariableElement<Q>> parametersBuilder();
        public abstract ImmutableList.Builder<QualifiedType<Q>> thrownTypesBuilder();

        public final Builder<Q> addTypeParameter(QualifiedTypeParameterElement<Q> typeParameter) {
            typeParametersBuilder().add(typeParameter);
            return this;
        }

        public final Builder<Q> addTypeParameters(Iterable<QualifiedTypeParameterElement<Q>> typeParameters) {
            typeParametersBuilder().addAll(typeParameters);
            return this;
        }

        public final Builder<Q> addParameter(QualifiedVariableElement<Q> parameter) {
            parametersBuilder().add(parameter);
            return this;
        }

        public final Builder<Q> addParameters(Iterable<QualifiedVariableElement<Q>> parameters) {
            parametersBuilder().addAll(parameters);
            return this;
        }

        public final Builder<Q> addThrownType(QualifiedType<Q> thrownType) {
            thrownTypesBuilder().add(thrownType);
            return this;
        }

        public final Builder<Q> addThrownTypes(Iterable<QualifiedType<Q>> thrownTypes) {
            thrownTypesBuilder().addAll(thrownTypes);
            return this;
        }

        protected abstract QualifiedExecutableElement<Q> autoBuild();

        public final QualifiedExecutableElement<Q> build() {
            QualifiedExecutableElement<Q> element = autoBuild();
            ExecutableElement rawElement = element.getJavaElement();
            Preconditions.checkState(
                    element.getQualifier() == null,
                    "No primary qualifier for executable element"
            );
            Preconditions.checkState(
                    rawElement.getParameters().size() == element.getParameters().size(),
                    "Parameters not matched"
            );
            Preconditions.checkState(
                    rawElement.getTypeParameters().size() == element.getTypeParameters().size(),
                    "Type parameters not matched"
            );
            Preconditions.checkState(
                    rawElement.getThrownTypes().size() == element.getThrownTypes().size(),
                    "Thrown types not matched"
            );
            return element;
        }
    }
}
