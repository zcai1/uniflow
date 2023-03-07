package org.uniflow.core.model.util;

import org.uniflow.core.model.qualifier.Qualifier;
import org.uniflow.core.model.type.PrimaryQualifiedType;
import org.uniflow.core.model.type.QualifiedArrayType;
import org.uniflow.core.model.type.QualifiedDeclaredType;
import org.uniflow.core.model.type.QualifiedIntersectionType;
import org.uniflow.core.model.type.QualifiedNullType;
import org.uniflow.core.model.type.QualifiedPrimitiveType;
import org.uniflow.core.model.type.QualifiedTypeVariable;
import org.uniflow.core.model.type.QualifiedUnionType;
import org.uniflow.core.model.type.QualifiedWildcardType;

public abstract class PrimaryQualifiedTypeScanner<Q extends Qualifier, R, P> extends QualifiedTypeScanner<Q, R, P> {

    protected abstract void visitPrimaryQualifiedType(PrimaryQualifiedType<Q> type, P p);

    @Override
    public R visitArray(QualifiedArrayType<Q> type, P p) {
        visitPrimaryQualifiedType(type, p);
        return super.visitArray(type, p);
    }

    @Override
    public R visitDeclared(QualifiedDeclaredType<Q> type, P p) {
        visitPrimaryQualifiedType(type, p);
        return super.visitDeclared(type, p);
    }

    @Override
    public R visitIntersection(QualifiedIntersectionType<Q> type, P p) {
        visitPrimaryQualifiedType(type, p);
        return super.visitIntersection(type, p);
    }

    @Override
    public R visitNull(QualifiedNullType<Q> type, P p) {
        visitPrimaryQualifiedType(type, p);
        return super.visitNull(type, p);
    }

    @Override
    public R visitPrimitive(QualifiedPrimitiveType<Q> type, P p) {
        visitPrimaryQualifiedType(type, p);
        return super.visitPrimitive(type, p);
    }

    @Override
    public R visitTypeVariable(QualifiedTypeVariable<Q> type, P p) {
        visitPrimaryQualifiedType(type, p);
        return super.visitTypeVariable(type, p);
    }

    @Override
    public R visitUnion(QualifiedUnionType<Q> type, P p) {
        visitPrimaryQualifiedType(type, p);
        return super.visitUnion(type, p);
    }

    @Override
    public R visitWildcard(QualifiedWildcardType<Q> type, P p) {
        visitPrimaryQualifiedType(type, p);
        return super.visitWildcard(type, p);
    }
}
