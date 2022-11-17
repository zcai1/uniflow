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

import java.util.List;

public class QualifiedTypeScanner<Q extends Qualifier, R, P> implements QualifiedTypeVisitor<Q, R, P> {

    protected final R DEFAULT_VALUE;

    // TODO(generics): handle recursive types
    // protected final IdentityHashMap<QualifiedType<Q>, R> visited;

    protected QualifiedTypeScanner() {
        this.DEFAULT_VALUE = null;
    }

    protected QualifiedTypeScanner(R defaultValue) {
        this.DEFAULT_VALUE = defaultValue;
    }

    protected R defaultAction(QualifiedType<Q> type, P p) {
        return DEFAULT_VALUE;
    }

    protected R scan(QualifiedType<Q> type, P p) {
        return (type == null) ? null : type.accept(this, p);
    }

    protected R scan(Iterable<? extends QualifiedType<Q>> types, P p) {
        R r = DEFAULT_VALUE;
        if (types != null) {
            boolean first = true;
            for (QualifiedType<Q> type : types) {
                r = (first ? scan(type, p) : scanAndReduce(type, p, r));
                first = false;
            }
        }
        return r;
    }

    protected R scanAndReduce(QualifiedType<Q> type, P p, R r) {
        return reduce(scan(type, p), r);
    }

    protected R scanAndReduce(Iterable<? extends QualifiedType<Q>> types, P p, R r) {
        return reduce(scan(types, p), r);
    }

    public R reduce(R r1, R r2) {
        return r1;
    }

    @Override
    public R visitArray(QualifiedArrayType<Q> type, P p) {
        return scan(type.getComponentType(), p);
    }

    @Override
    public R visitDeclared(QualifiedDeclaredType<Q> type, P p) {
        R r = scan(type.getEnclosingType(), p);
        r = scanAndReduce(type.getTypeArguments(), p, r);
        return r;
    }

    @Override
    public R visitExecutable(QualifiedExecutableType<Q> type, P p) {
        R r = scan(type.getReturnType(), p);
        r = scanAndReduce(type.getParameterTypes(), p, r);
        r = scanAndReduce(type.getReceiverType(), p, r);
        r = scanAndReduce(type.getThrownTypes(), p, r);
        return r;
    }

    @Override
    public R visitIntersection(QualifiedIntersectionType<Q> type, P p) {
        return scan(type.getBounds(), p);
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
        // TODO(generics): implementation
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitUnion(QualifiedUnionType<Q> type, P p) {
        return scan(type.getAlternatives(), p);
    }

    @Override
    public R visitWildcard(QualifiedWildcardType<Q> type, P p) {
        // TODO(generics): implementation
        throw new UnsupportedOperationException();
    }
}
