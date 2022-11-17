package org.cfginference.core.model.util;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.type.QualifiedArrayType;
import org.cfginference.core.model.type.QualifiedDeclaredType;
import org.cfginference.core.model.type.QualifiedExecutableType;
import org.cfginference.core.model.type.QualifiedIntersectionType;
import org.cfginference.core.model.type.QualifiedNoType;
import org.cfginference.core.model.type.QualifiedNullType;
import org.cfginference.core.model.type.QualifiedPrimitiveType;
import org.cfginference.core.model.type.QualifiedType;
import org.cfginference.core.model.type.QualifiedTypeVariable;
import org.cfginference.core.model.type.QualifiedUnionType;
import org.cfginference.core.model.type.QualifiedWildcardType;

public class SimpleQualifiedTypeVisitor<Q extends Qualifier, R, P> implements QualifiedTypeVisitor<Q, R, P> {

    protected final R DEFAULT_VALUE;

    protected SimpleQualifiedTypeVisitor() {
        DEFAULT_VALUE = null;
    }

    protected SimpleQualifiedTypeVisitor(R defaultValue) {
        DEFAULT_VALUE = defaultValue;
    }

    protected R defaultAction(QualifiedType<Q> type, P p) {
        return DEFAULT_VALUE;
    }

    public R visit(QualifiedType<Q> type, P p) {
        return (type == null) ? null : type.accept(this, p);
    }

    @Override
    public R visitArray(QualifiedArrayType<Q> type, P p) {
        return defaultAction(type, p);
    }

    @Override
    public R visitDeclared(QualifiedDeclaredType<Q> type, P p) {
        return defaultAction(type, p);
    }

    @Override
    public R visitExecutable(QualifiedExecutableType<Q> type, P p) {
        return defaultAction(type, p);
    }

    @Override
    public R visitIntersection(QualifiedIntersectionType<Q> type, P p) {
        return defaultAction(type, p);
    }

    @Override
    public R visitNo(QualifiedNoType<Q> type, P p) {
        return defaultAction(type, p);
    }

    @Override
    public R visitNull(QualifiedNullType<Q> type, P p) {
        return defaultAction(type, p);
    }

    @Override
    public R visitPrimitive(QualifiedPrimitiveType<Q> type, P p) {
        return defaultAction(type, p);
    }

    @Override
    public R visitTypeVariable(QualifiedTypeVariable<Q> type, P p) {
        return defaultAction(type, p);
    }

    @Override
    public R visitUnion(QualifiedUnionType<Q> type, P p) {
        return defaultAction(type, p);
    }

    @Override
    public R visitWildcard(QualifiedWildcardType<Q> type, P p) {
        return defaultAction(type, p);
    }
}
