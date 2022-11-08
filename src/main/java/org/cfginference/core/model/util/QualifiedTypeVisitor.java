package org.cfginference.core.model.util;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.type.QualifiedArrayType;
import org.cfginference.core.model.type.QualifiedDeclaredType;
import org.cfginference.core.model.type.QualifiedExecutableType;
import org.cfginference.core.model.type.QualifiedIntersectionType;
import org.cfginference.core.model.type.QualifiedNoType;
import org.cfginference.core.model.type.QualifiedNullType;
import org.cfginference.core.model.type.QualifiedPrimitiveType;
import org.cfginference.core.model.type.QualifiedTypeVariable;
import org.cfginference.core.model.type.QualifiedUnionType;
import org.cfginference.core.model.type.QualifiedWildcardType;

public interface QualifiedTypeVisitor<Q extends Qualifier, R, P> {
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
