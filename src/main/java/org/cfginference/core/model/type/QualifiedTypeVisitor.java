package org.cfginference.core.model.type;

import org.cfginference.core.model.qualifier.Qualifier;

public interface QualifiedTypeVisitor<Q extends Qualifier, R, P> {
    default R visit(QualifiedType<Q> type) {
        return visit(type, null);
    }

    default R visit(QualifiedType<Q> type, P p) {
        return type.accept(this, p);
    }

    R visitArray(QualifiedArrayType<Q> type, P p);

    R visitDeclared(QualifiedDeclaredType<Q> type, P p);

    R visitExecutable(QualifiedExecutableType<Q> type, P p);

    R visitIntersection(QualifiedIntersectionType<Q> type, P p);

    R visitNo(QualifiedNoType<Q> type, P p);

    R visitNull(QualifiedNullType<Q> type, P p);

    R visitPrimitive(QualifiedPrimitiveType<Q> type, P p);

    R visitTypeVariable(QualifiedTypeVariable<Q> type, P p);

    R visitUnion(QualifiedUnionType<Q> type, P p);

    R visitWildcard(QualifiedWildcardType<Q> type, P p);
}
