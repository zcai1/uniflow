package org.cfginference.core.model.element;


import com.google.auto.value.AutoValue;
import org.cfginference.core.model.qualifier.Qualifier;

import javax.lang.model.element.RecordComponentElement;

@AutoValue
public abstract class QualifiedRecordComponentElement<Q extends Qualifier> extends QualifiedElement<Q> {
    @Override
    public abstract RecordComponentElement getJavaElement();

    public abstract QualifiedElement<Q> getEnclosingElement();

    public abstract QualifiedExecutableElement<Q> getAccessor();

    @Override
    public final QualifiedRecordComponentElement<Q> withQualifier(Q qualifier) {
        return toBuilder().setQualifier(qualifier).build();
    }

    @Override
    public final <R, P> R accept(QualifiedElementVisitor<Q, R, P> v, P p) {
        return v.visitRecordComponent(this, p);
    }

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedRecordComponentElement.Builder<>();
    }

    public abstract Builder<Q> toBuilder();

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> {
        public abstract Builder<Q> setQualifier(Q qualifier);
        public abstract Builder<Q> setJavaElement(RecordComponentElement element);
        public abstract Builder<Q> setEnclosingElement(QualifiedElement<Q> enclosingElement);
        public abstract Builder<Q> setAccessor(QualifiedExecutableElement<Q> accessor);
        public abstract QualifiedRecordComponentElement<Q> build();
    }
}
