package org.cfginference.core.model.util;

import com.google.common.base.Preconditions;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class DoubleQualifiedTypeVisitor<Q1 extends Qualifier, Q2 extends Qualifier, R>
    extends SimpleQualifiedTypeVisitor<Q1, R, QualifiedType<Q2>> {

    protected List<R> visit(List<? extends QualifiedType<Q1>> types1, List<? extends QualifiedType<Q2>> types2) {
        Preconditions.checkArgument(types1.size() == types2.size());

        List<R> rs = new ArrayList<>(types1.size());
        Iterator<? extends QualifiedType<Q1>> iter1 = types1.iterator();
        Iterator<? extends QualifiedType<Q2>> iter2 = types2.iterator();
        while (iter1.hasNext()) {
            rs.add(visit(iter1.next(), iter2.next()));
        }
        return rs;
    }

    @Override
    public final R visitArray(QualifiedArrayType<Q1> type1, QualifiedType<Q2> p) {
        assert p instanceof QualifiedArrayType : p;

        QualifiedArrayType<Q2> type2 = (QualifiedArrayType<Q2>) p;
        return visitArray(type1, type2);
    }

    public R visitArray(QualifiedArrayType<Q1> type1, QualifiedArrayType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    @Override
    public final R visitDeclared(QualifiedDeclaredType<Q1> type1, QualifiedType<Q2> p) {
        assert p instanceof QualifiedDeclaredType: p;

        QualifiedDeclaredType<Q2> type2 = (QualifiedDeclaredType<Q2>) p;
        return visitDeclared(type1, type2);
    }

    public R visitDeclared(QualifiedDeclaredType<Q1> type1, QualifiedDeclaredType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    @Override
    public final R visitExecutable(QualifiedExecutableType<Q1> type1, QualifiedType<Q2> p) {
        assert p instanceof QualifiedExecutableType: p;

        QualifiedExecutableType<Q2> type2 = (QualifiedExecutableType<Q2>) p;
        return visitExecutable(type1, type2);
    }

    public R visitExecutable(QualifiedExecutableType<Q1> type1, QualifiedExecutableType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    @Override
    public final R visitIntersection(QualifiedIntersectionType<Q1> type1, QualifiedType<Q2> p) {
        assert p instanceof QualifiedIntersectionType: p;

        QualifiedIntersectionType<Q2> type2 = (QualifiedIntersectionType<Q2>) p;
        return visitIntersection(type1, type2);
    }

    public R visitIntersection(QualifiedIntersectionType<Q1> type1, QualifiedIntersectionType<Q2> type2) {
        return defaultAction(type1, type2);
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
        return defaultAction(type1, type2);
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
