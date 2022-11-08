package org.cfginference.core.model.element;


import com.google.auto.value.AutoValue;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.type.QualifiedType;
import org.cfginference.core.model.util.QualifiedElementVisitor;

import javax.lang.model.element.RecordComponentElement;

@AutoValue
public abstract class QualifiedRecordComponentElement<Q extends Qualifier> extends QualifiedElement<Q> {
    @Override
    public abstract RecordComponentElement getJavaElement();

    public abstract QualifiedType<Q> getType();

    public abstract QualifiedExecutableElement<Q> getAccessor();

    @Override
    public final <R, P> R accept(QualifiedElementVisitor<Q, R, P> v, P p) {
        return v.visitRecordComponent(this, p);
    }

    public static <Q extends Qualifier> Builder<Q> builder() {
        return new AutoValue_QualifiedRecordComponentElement.Builder<>();
    }

    @Override
    public abstract Builder<Q> toBuilder();

    @AutoValue.Builder
    public abstract static class Builder<Q extends Qualifier> extends QualifiedElement.Builder<Q> {

        public abstract Builder<Q> setType(QualifiedType<Q> type);

        public abstract Builder<Q> setJavaElement(RecordComponentElement element);

        public abstract Builder<Q> setAccessor(QualifiedExecutableElement<Q> accessor);

        @Override
        public abstract QualifiedRecordComponentElement<Q> build();
    }
}
