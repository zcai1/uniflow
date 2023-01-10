package org.cfginference.core.model.type;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.util.QualifiedTypeVisitor;
import org.cfginference.core.model.util.TypeQualifierComparator;
import org.cfginference.core.model.util.TypeStructureComparator;
import org.cfginference.core.model.util.formatter.DefaultQualifiedTypeFormatter;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public abstract class QualifiedType<Q extends Qualifier> {

    private static final TypeStructureComparator structureComparator = new TypeStructureComparator();

    private static final TypeQualifierComparator qualifierComparator = new TypeQualifierComparator();

    public abstract TypeMirror getJavaType();

    public final TypeKind getKind() {
        return getJavaType().getKind();
    }

    public abstract Builder<Q> toBuilder();

    public abstract <R, P> R accept(QualifiedTypeVisitor<Q, R, P> v, P p);

    public static abstract class Builder<Q extends Qualifier> {

        public abstract QualifiedType<Q> build();
    }

    @Override
    public String toString() {
        return DefaultQualifiedTypeFormatter.getInstance().format(this);
    }

    public boolean structurallyEquals(Types types, QualifiedType<Q> other) {
        if (other == null) {
            return false;
        }
        return structureComparator.areEqual(this, other)
                && types.isSameType(this.getJavaType(), other.getJavaType());
    }

    public boolean equals(Types types, QualifiedType<Q> other) {
        if (this == other) {
            return true;
        }
        return structurallyEquals(types, other) && qualifierComparator.areEqual(this, other);
    }
}
