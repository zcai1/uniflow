package org.cfginference.core.model.util;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.type.PrimaryQualifiedType;
import org.cfginference.core.model.type.QualifiedArrayType;
import org.cfginference.core.model.type.QualifiedDeclaredType;
import org.cfginference.core.model.type.QualifiedExecutableType;
import org.cfginference.core.model.type.QualifiedIntersectionType;
import org.cfginference.core.model.type.QualifiedNoType;
import org.cfginference.core.model.type.QualifiedNullType;
import org.cfginference.core.model.type.QualifiedPrimitiveType;
import org.cfginference.core.model.type.QualifiedType;
import org.cfginference.core.model.type.QualifiedUnionType;

public abstract class QualifiedTypeCombiner<Q1 extends Qualifier, Q2 extends Qualifier, RQ extends Qualifier>
        extends DoubleQualifiedTypeVisitor<Q1, Q2, QualifiedType<RQ>> {

    protected abstract RQ defaultAction(PrimaryQualifiedType<Q1> type1, PrimaryQualifiedType<Q2> type2);

    @Override
    public QualifiedArrayType<RQ> visitArray(QualifiedArrayType<Q1> type1, QualifiedArrayType<Q2> type2) {
        return QualifiedArrayType.<RQ>builder()
                .setJavaType(type1.getJavaType())
                .setComponentType(visit(type1.getComponentType(), type2.getComponentType()))
                .setQualifier(defaultAction(type1, type2))
                .build();
    }

    @Override
    public QualifiedDeclaredType<RQ> visitDeclared(QualifiedDeclaredType<Q1> type1, QualifiedDeclaredType<Q2> type2) {
        return QualifiedDeclaredType.<RQ>builder()
                .setJavaType(type1.getJavaType())
                .setEnclosingType(visit(type1.getEnclosingType(), type2.getEnclosingType()))
                .setTypeArguments(visit(type1.getTypeArguments(), type2.getTypeArguments()))
                .setQualifier(defaultAction(type1, type2))
                .build();
    }

    @Override
    public QualifiedExecutableType<RQ> visitExecutable(QualifiedExecutableType<Q1> type1, QualifiedExecutableType<Q2> type2) {
        return QualifiedExecutableType.<RQ>builder()
                .setJavaType(type1.getJavaType())
                .setReceiverType(visit(type1.getReceiverType(), type2.getReceiverType()))
                .setReturnType(visit(type1.getReturnType(), type2.getReturnType()))
                .setParameterTypes(visit(type1.getParameterTypes(), type2.getParameterTypes()))
                .setThrownTypes(visit(type1.getThrownTypes(), type2.getThrownTypes()))
                .build();
    }

    @Override
    public QualifiedIntersectionType<RQ> visitIntersection(QualifiedIntersectionType<Q1> type1, QualifiedIntersectionType<Q2> type2) {
        return QualifiedIntersectionType.<RQ>builder()
                .setJavaType(type1.getJavaType())
                .setBounds(visit(type1.getBounds(), type2.getBounds()))
                .setQualifier(defaultAction(type1, type2))
                .build();
    }

    @Override
    public QualifiedNoType<RQ> visitNo(QualifiedNoType<Q1> type1, QualifiedNoType<Q2> type2) {
        return QualifiedNoType.<RQ>builder()
                .setJavaType(type1.getJavaType())
                .build();
    }

    @Override
    public QualifiedNullType<RQ> visitNull(QualifiedNullType<Q1> type1, QualifiedNullType<Q2> type2) {
        return QualifiedNullType.<RQ>builder()
                .setJavaType(type1.getJavaType())
                .build();
    }

    @Override
    public QualifiedPrimitiveType<RQ> visitPrimitive(QualifiedPrimitiveType<Q1> type1, QualifiedPrimitiveType<Q2> type2) {
        return QualifiedPrimitiveType.<RQ>builder()
                .setJavaType(type1.getJavaType())
                .setQualifier(defaultAction(type1, type2))
                .build();
    }

    @Override
    public QualifiedUnionType<RQ> visitUnion(QualifiedUnionType<Q1> type1, QualifiedUnionType<Q2> type2) {
        return QualifiedUnionType.<RQ>builder()
                .setJavaType(type1.getJavaType())
                .setAlternatives(visit(type1.getAlternatives(), type2.getAlternatives()))
                .setQualifier(defaultAction(type1, type2))
                .build();
    }
}
