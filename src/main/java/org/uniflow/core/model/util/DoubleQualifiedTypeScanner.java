package org.uniflow.core.model.util;

import org.uniflow.core.model.qualifier.Qualifier;
import org.uniflow.core.model.type.QualifiedArrayType;
import org.uniflow.core.model.type.QualifiedDeclaredType;
import org.uniflow.core.model.type.QualifiedExecutableType;
import org.uniflow.core.model.type.QualifiedIntersectionType;
import org.uniflow.core.model.type.QualifiedNoType;
import org.uniflow.core.model.type.QualifiedNullType;
import org.uniflow.core.model.type.QualifiedPrimitiveType;
import org.uniflow.core.model.type.QualifiedType;
import org.uniflow.core.model.type.QualifiedTypeVariable;
import org.uniflow.core.model.type.QualifiedUnionType;
import org.uniflow.core.model.type.QualifiedWildcardType;

import java.util.Collection;
import java.util.Iterator;

public abstract class DoubleQualifiedTypeScanner<Q1 extends Qualifier, Q2 extends Qualifier, R>
        extends QualifiedTypeScanner<Q1, R, QualifiedType<Q2>> {

    protected DoubleQualifiedTypeScanner() {
        super();
    }

    protected DoubleQualifiedTypeScanner(R defaultValue) {
        super(defaultValue);
    }

    protected R scan(Collection<? extends QualifiedType<Q1>> types1, Collection<? extends QualifiedType<Q2>> types2) {
        R r = DEFAULT_VALUE;
        if (types1 != null && types2 != null) {
            boolean first = true;
            Iterator<? extends QualifiedType<Q1>> it1 = types1.iterator();
            Iterator<? extends QualifiedType<Q2>> it2 = types2.iterator();
            while (it1.hasNext() && it2.hasNext()) {
                QualifiedType<Q1> t1 = it1.next();
                QualifiedType<Q2> t2 = it2.next();
                r = first ? scan(t1, t2) : scanAndReduce(t1, t2, r);
                first = false;
            }
            assert !it1.hasNext() && !it2.hasNext(): "Scanning types of different sizes";
        }
        return r;
    }

    @Override
    protected final R scanAndReduce(Collection<? extends QualifiedType<Q1>> types, QualifiedType<Q2> type, R r) {
        // unused
        throw new UnsupportedOperationException();
    }

    protected R scanAndReduce(Collection<? extends QualifiedType<Q1>> types1,
                              Collection<? extends QualifiedType<Q2>> types2,
                              R r) {
        return reduce(scan(types1, types2), r);
    }

    @Override
    public final R visitArray(QualifiedArrayType<Q1> type1, QualifiedType<Q2> p) {
        assert p instanceof QualifiedArrayType : p;

        QualifiedArrayType<Q2> type2 = (QualifiedArrayType<Q2>) p;
        return visitArray(type1, type2);
    }

    public R visitArray(QualifiedArrayType<Q1> type1, QualifiedArrayType<Q2> type2) {
        return scan(type1.getComponentType(), type2.getComponentType());
    }

    @Override
    public final R visitDeclared(QualifiedDeclaredType<Q1> type1, QualifiedType<Q2> p) {
        assert p instanceof QualifiedDeclaredType: p;

        QualifiedDeclaredType<Q2> type2 = (QualifiedDeclaredType<Q2>) p;
        return visitDeclared(type1, type2);
    }

    public R visitDeclared(QualifiedDeclaredType<Q1> type1, QualifiedDeclaredType<Q2> type2) {
        R r = scan(type1.getTypeArguments(), type2.getTypeArguments());
        r = scanAndReduce(type1.getEnclosingType(), type2.getEnclosingType(), r);
        return r;
    }

    @Override
    public final R visitExecutable(QualifiedExecutableType<Q1> type1, QualifiedType<Q2> p) {
        assert p instanceof QualifiedExecutableType: p;

        QualifiedExecutableType<Q2> type2 = (QualifiedExecutableType<Q2>) p;
        return visitExecutable(type1, type2);
    }

    public R visitExecutable(QualifiedExecutableType<Q1> type1, QualifiedExecutableType<Q2> type2) {
        R r = scan(type1.getReturnType(), type2.getReturnType());
        r = scanAndReduce(type1.getParameterTypes(), type2.getParameterTypes(), r);
        r = scanAndReduce(type1.getReceiverType(), type2.getReceiverType(), r);
        r = scanAndReduce(type1.getThrownTypes(), type2.getThrownTypes(), r);
        return r;
    }

    @Override
    public final R visitIntersection(QualifiedIntersectionType<Q1> type1, QualifiedType<Q2> p) {
        assert p instanceof QualifiedIntersectionType: p;

        QualifiedIntersectionType<Q2> type2 = (QualifiedIntersectionType<Q2>) p;
        return visitIntersection(type1, type2);
    }

    public R visitIntersection(QualifiedIntersectionType<Q1> type1, QualifiedIntersectionType<Q2> type2) {
        return scan(type1.getBounds(), type2.getBounds());
    }

    @Override
    public final R visitNo(QualifiedNoType<Q1> type1, QualifiedType<Q2> p) {
        assert p instanceof QualifiedNoType: p;

        QualifiedNoType<Q2> type2 = (QualifiedNoType<Q2>) p;
        return visitNo(type1, type2);
    }

    public R visitNo(QualifiedNoType<Q1> type1, QualifiedNoType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    @Override
    public final R visitNull(QualifiedNullType<Q1> type1, QualifiedType<Q2> p) {
        assert p instanceof QualifiedNullType: p;

        QualifiedNullType<Q2> type2 = (QualifiedNullType<Q2>) p;
        return visitNull(type1, type2);
    }

    public R visitNull(QualifiedNullType<Q1> type1, QualifiedNullType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    @Override
    public final R visitPrimitive(QualifiedPrimitiveType<Q1> type1, QualifiedType<Q2> p) {
        assert p instanceof QualifiedPrimitiveType: p;

        QualifiedPrimitiveType<Q2> type2 = (QualifiedPrimitiveType<Q2>) p;
        return visitPrimitive(type1, type2);
    }

    public R visitPrimitive(QualifiedPrimitiveType<Q1> type1, QualifiedPrimitiveType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    @Override
    public final R visitTypeVariable(QualifiedTypeVariable<Q1> type1, QualifiedType<Q2> p) {
        assert p instanceof QualifiedTypeVariable: p;

        QualifiedTypeVariable<Q2> type2 = (QualifiedTypeVariable<Q2>) p;
        return visitTypeVariable(type1, type2);
    }

    public R visitTypeVariable(QualifiedTypeVariable<Q1> type1, QualifiedTypeVariable<Q2> type2) {
        // TODO(generics): implementation
        throw new UnsupportedOperationException();
    }

    @Override
    public final R visitUnion(QualifiedUnionType<Q1> type1, QualifiedType<Q2> p) {
        assert p instanceof QualifiedUnionType: p;

        QualifiedUnionType<Q2> type2 = (QualifiedUnionType<Q2>) p;
        return visitUnion(type1, type2);
    }

    public R visitUnion(QualifiedUnionType<Q1> type1, QualifiedUnionType<Q2> type2) {
        return scan(type1.getAlternatives(), type2.getAlternatives());
    }

    @Override
    public final R visitWildcard(QualifiedWildcardType<Q1> type1, QualifiedType<Q2> p) {
        assert p instanceof QualifiedWildcardType: p;

        QualifiedWildcardType<Q2> type2 = (QualifiedWildcardType<Q2>) p;
        return visitWildcard(type1, type2);
    }

    public R visitWildcard(QualifiedWildcardType<Q1> type1, QualifiedWildcardType<Q2> type2) {
        // TODO(generics): implementation
        throw new UnsupportedOperationException();
    }
}
