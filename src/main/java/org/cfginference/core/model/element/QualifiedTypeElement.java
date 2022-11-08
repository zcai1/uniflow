package org.cfginference.core.model.element;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.type.QualifiedType;
import org.cfginference.core.model.util.QualifiedElementVisitor;

import javax.lang.model.element.TypeElement;

@AutoValue
public abstract class QualifiedTypeElement<Q extends Qualifier> extends PrimaryQualifiedElement<Q> {
    @Override
    public abstract TypeElement getJavaElement();

    public abstract QualifiedType<Q> getSuperClass();

    public abstract ImmutableList<QualifiedType<Q>> getInterfaces();

    public abstract ImmutableList<QualifiedTypeParameterElement<Q>> getTypeParameters();

    public abstract ImmutableList<QualifiedRecordComponentElement<Q>> getRecordComponents();

    @Override
    public final <R, P> R accept(QualifiedElementVisitor<Q, R, P> v, P p) {
        return v.visitType(this, p);
    }

    @Override
    public final QualifiedTypeElement<Q> withQualifier(Q qualifier) {
        return toBuilder().setQualifier(qualifier).build();
    }

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedTypeElement.Builder<>();
    }

    @Override
    public abstract Builder<Q> toBuilder();

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> extends PrimaryQualifiedElement.Builder<Q> {
        @Override
        public abstract Builder<Q> setQualifier(Q qualifier);

        public abstract Builder<Q> setJavaElement(TypeElement element);

        public abstract Builder<Q> setSuperClass(QualifiedType<Q> superClass);

        public abstract ImmutableList.Builder<QualifiedType<Q>> interfacesBuilder();

        public abstract ImmutableList.Builder<QualifiedTypeParameterElement<Q>> typeParametersBuilder();

        public abstract ImmutableList.Builder<QualifiedRecordComponentElement<Q>> recordComponentsBuilder();

        public final Builder<Q> addInterface(QualifiedType<Q> qualifiedInterface) {
            interfacesBuilder().add(qualifiedInterface);
            return this;
        }

        public final Builder<Q> addInterfaces(Iterable<QualifiedType<Q>> qualifiedInterfaces) {
            interfacesBuilder().addAll(qualifiedInterfaces);
            return this;
        }

        public final Builder<Q> addTypeParameter(QualifiedTypeParameterElement<Q> typeParameter) {
            typeParametersBuilder().add(typeParameter);
            return this;
        }

        public final Builder<Q> addTypeParameters(Iterable<QualifiedTypeParameterElement<Q>> typeParameters) {
            typeParametersBuilder().addAll(typeParameters);
            return this;
        }

        public final Builder<Q> addRecordComponent(QualifiedRecordComponentElement<Q> recordComponent) {
            recordComponentsBuilder().add(recordComponent);
            return this;
        }

        public final Builder<Q> addRecordComponents(Iterable<QualifiedRecordComponentElement<Q>> recordComponents) {
            recordComponentsBuilder().addAll(recordComponents);
            return this;
        }

        protected abstract QualifiedTypeElement<Q> autoBuild();

        @Override
        public final QualifiedTypeElement<Q> build() {
            QualifiedTypeElement<Q> element = autoBuild();
            Preconditions.checkState(
                    element.getInterfaces().size() == element.getJavaElement().getInterfaces().size(),
                    "Interfaces not matched"
            );
            Preconditions.checkState(
                    element.getTypeParameters().size() == element.getJavaElement().getTypeParameters().size(),
                    "Type parameters not matched"
            );
            Preconditions.checkState(
                    element.getRecordComponents().size() == element.getJavaElement().getRecordComponents().size(),
                    "Type parameters not matched"
            );
            return element;
        }
    }
}
