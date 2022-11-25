package org.cfginference.core.model.util;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.type.QualifiedArrayType;
import org.cfginference.core.model.type.QualifiedDeclaredType;
import org.cfginference.core.model.type.QualifiedIntersectionType;
import org.cfginference.core.model.type.QualifiedNullType;
import org.cfginference.core.model.type.QualifiedPrimitiveType;
import org.cfginference.core.model.type.QualifiedType;
import org.cfginference.core.model.type.QualifiedUnionType;

public class TypeQualifierComparator<Q extends Qualifier> extends DoubleQualifiedTypeScanner<Q, Q, Boolean> {

    public TypeQualifierComparator() {
        super(true);
    }

    @Override
    protected Boolean scanAndReduce(QualifiedType<Q> type1, QualifiedType<Q> type2, Boolean r) {
        return r && scan(type1, type2);
    }

    @Override
    protected Boolean scanAndReduce(Iterable<? extends QualifiedType<Q>> types1,
                                    Iterable<? extends QualifiedType<Q>> types2,
                                    Boolean r) {
        return r && scan(types1, types2);
    }

    @Override
    public final Boolean reduce(Boolean r1, Boolean r2) {
        throw new UnsupportedOperationException();
    }

    protected boolean areSameQualifier(Q q1, Q q2) {
        return q1.equals(q2);
    }

    @Override
    public Boolean visitArray(QualifiedArrayType<Q> type1, QualifiedArrayType<Q> type2) {
        return areSameQualifier(type1.getQualifier(), type2.getQualifier())
                && super.visitArray(type1, type2);
    }

    @Override
    public Boolean visitDeclared(QualifiedDeclaredType<Q> type1, QualifiedDeclaredType<Q> type2) {
        return areSameQualifier(type1.getQualifier(), type2.getQualifier())
                && super.visitDeclared(type1, type2);
    }

    @Override
    public Boolean visitIntersection(QualifiedIntersectionType<Q> type1, QualifiedIntersectionType<Q> type2) {
        return areSameQualifier(type1.getQualifier(), type2.getQualifier())
                && super.visitIntersection(type1, type2);
    }

    @Override
    public Boolean visitNull(QualifiedNullType<Q> type1, QualifiedNullType<Q> type2) {
        return areSameQualifier(type1.getQualifier(), type2.getQualifier())
                && super.visitNull(type1, type2);
    }

    @Override
    public Boolean visitPrimitive(QualifiedPrimitiveType<Q> type1, QualifiedPrimitiveType<Q> type2) {
        return areSameQualifier(type1.getQualifier(), type2.getQualifier())
                && super.visitPrimitive(type1, type2);
    }

    @Override
    public Boolean visitUnion(QualifiedUnionType<Q> type1, QualifiedUnionType<Q> type2) {
        return areSameQualifier(type1.getQualifier(), type2.getQualifier())
                && super.visitUnion(type1, type2);
    }
}
