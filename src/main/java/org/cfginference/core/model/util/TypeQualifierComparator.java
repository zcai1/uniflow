package org.cfginference.core.model.util;

import com.google.common.base.Preconditions;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.type.QualifiedArrayType;
import org.cfginference.core.model.type.QualifiedDeclaredType;
import org.cfginference.core.model.type.QualifiedIntersectionType;
import org.cfginference.core.model.type.QualifiedNullType;
import org.cfginference.core.model.type.QualifiedPrimitiveType;
import org.cfginference.core.model.type.QualifiedType;
import org.cfginference.core.model.type.QualifiedUnionType;

import java.util.Collection;

public class TypeQualifierComparator extends DoubleQualifiedTypeScanner<Qualifier, Qualifier, Boolean> {

    public TypeQualifierComparator() {
        super(true);
    }
    
    public boolean areEqual(QualifiedType type1, QualifiedType type2) {
        return scan(type1, type2);
    }

    @Override
    public Boolean scan(QualifiedType<Qualifier> type1, QualifiedType<Qualifier> type2) {
        Preconditions.checkNotNull(type1);
        Preconditions.checkNotNull(type2);
        return super.scan(type1, type2);
    }

    @Override
    protected Boolean scanAndReduce(QualifiedType<Qualifier> type1, QualifiedType<Qualifier> type2, Boolean r) {
        return r && scan(type1, type2);
    }

    @Override
    protected Boolean scanAndReduce(Collection<? extends QualifiedType<Qualifier>> types1,
                                    Collection<? extends QualifiedType<Qualifier>> types2,
                                    Boolean r) {
        return r && scan(types1, types2);
    }

    @Override
    public final Boolean reduce(Boolean r1, Boolean r2) {
        throw new UnsupportedOperationException();
    }

    protected boolean areSameQualifier(Qualifier q1, Qualifier q2) {
        return q1.equals(q2);
    }

    @Override
    public Boolean visitArray(QualifiedArrayType<Qualifier> type1, QualifiedArrayType<Qualifier> type2) {
        return areSameQualifier(type1.getQualifier(), type2.getQualifier())
                && super.visitArray(type1, type2);
    }

    @Override
    public Boolean visitDeclared(QualifiedDeclaredType<Qualifier> type1, QualifiedDeclaredType<Qualifier> type2) {
        return areSameQualifier(type1.getQualifier(), type2.getQualifier())
                && super.visitDeclared(type1, type2);
    }

    @Override
    public Boolean visitIntersection(QualifiedIntersectionType<Qualifier> type1, QualifiedIntersectionType<Qualifier> type2) {
        return areSameQualifier(type1.getQualifier(), type2.getQualifier())
                && super.visitIntersection(type1, type2);
    }

    @Override
    public Boolean visitNull(QualifiedNullType<Qualifier> type1, QualifiedNullType<Qualifier> type2) {
        return areSameQualifier(type1.getQualifier(), type2.getQualifier())
                && super.visitNull(type1, type2);
    }

    @Override
    public Boolean visitPrimitive(QualifiedPrimitiveType<Qualifier> type1, QualifiedPrimitiveType<Qualifier> type2) {
        return areSameQualifier(type1.getQualifier(), type2.getQualifier())
                && super.visitPrimitive(type1, type2);
    }

    @Override
    public Boolean visitUnion(QualifiedUnionType<Qualifier> type1, QualifiedUnionType<Qualifier> type2) {
        return areSameQualifier(type1.getQualifier(), type2.getQualifier())
                && super.visitUnion(type1, type2);
    }
}
