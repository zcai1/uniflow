package org.cfginference.core.model.element;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.cfginference.core.model.type.QualifiedExecutableType;
import org.cfginference.core.model.type.QualifiedType;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.util.QualifiedElementVisitor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ExecutableType;

@AutoValue
public abstract class QualifiedExecutableElement<Q extends Qualifier> extends QualifiedElement<Q> {

    @Override
    public abstract ExecutableElement getJavaElement();

    public abstract ImmutableList<QualifiedTypeParameterElement<Q>> getTypeParameters();

    public abstract ImmutableList<QualifiedVariableElement<Q>> getParameters();

    public abstract ImmutableList<QualifiedType<Q>> getThrownTypes();

    /**
     * Unlike the definition of {@link ExecutableElement#getReturnType()}, this may return the type
     * being instantiated if it's a constructor.
     *
     * @return the return type of this executable, or the type being instantiated if it's a constructor
     */
    public abstract QualifiedType<Q> getReturnType();

    public abstract QualifiedType<Q> getReceiverType();

    @Override
    public final <R, P> R accept(QualifiedElementVisitor<Q, R, P> v, P p) {
        return v.visitExecutable(this, p);
    }

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedExecutableElement.Builder<>();
    }

    @Override
    public abstract Builder<Q> toBuilder();

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> extends QualifiedElement.Builder<Q> {

        public abstract Builder<Q> setJavaElement(ExecutableElement element);

        public abstract Builder<Q> setReturnType(QualifiedType<Q> type);

        public abstract Builder<Q> setReceiverType(QualifiedType<Q> type);

        public abstract ImmutableList.Builder<QualifiedTypeParameterElement<Q>> typeParametersBuilder();

        public abstract ImmutableList.Builder<QualifiedVariableElement<Q>> parametersBuilder();

        public abstract ImmutableList.Builder<QualifiedType<Q>> thrownTypesBuilder();

        public abstract Builder<Q> setTypeParameters(Iterable<QualifiedTypeParameterElement<Q>> typeParameters);

        public abstract Builder<Q> setParameters(Iterable<QualifiedVariableElement<Q>> parameters);

        public abstract Builder<Q> setThrownTypes(Iterable<QualifiedType<Q>> thrownTypes);

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

        @Override
        public final QualifiedExecutableElement<Q> build() {
            QualifiedExecutableElement<Q> element = autoBuild();
            ExecutableElement rawElement = element.getJavaElement();
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

    public QualifiedExecutableType<Q> asType() {
        return QualifiedExecutableType.<Q>builder()
                .setJavaType((ExecutableType) getJavaElement().asType())
                .setJavaElement(getJavaElement())
                .setParameterTypes(getParameters().stream().map(QualifiedVariableElement::getType).toList())
                .setReceiverType(getReceiverType())
                .setReturnType(getReturnType())
                .setThrownTypes(getThrownTypes())
                .build();
    }
}
