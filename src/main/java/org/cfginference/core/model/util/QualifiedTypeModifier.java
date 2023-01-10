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
import org.cfginference.core.model.type.QualifiedTypeVariable;
import org.cfginference.core.model.type.QualifiedUnionType;
import org.cfginference.core.model.type.QualifiedWildcardType;
import org.checkerframework.checker.nullness.qual.PolyNull;

import java.util.List;

// Returns a new QualifiedType with modified qualifiers: QualifiedType<Q1> -> QualifiedType<Q2>
public abstract class QualifiedTypeModifier<Q1 extends Qualifier, Q2 extends Qualifier, P>
        implements QualifiedTypeVisitor<Q1, QualifiedType<Q2>, P> {

    public QualifiedType<Q2> visit(QualifiedType<Q1> type, P p) {
        return (type == null) ? null : type.accept(this, p);
    }

    public List<QualifiedType<Q2>> visit(List<? extends QualifiedType<Q1>> types, P p) {
        return types.stream().map(t -> this.visit(t, p)).toList();
    }

    protected abstract Q2 defaultAction(PrimaryQualifiedType<Q1> type, P p);

    @Override
    public QualifiedArrayType<Q2> visitArray(QualifiedArrayType<Q1> type, P p) {
        return QualifiedArrayType.<Q2>builder()
                .setJavaType(type.getJavaType())
                .setComponentType(visit(type.getComponentType(), p))
                .setQualifier(defaultAction(type, p))
                .build();
    }

    @Override
    public QualifiedDeclaredType<Q2> visitDeclared(QualifiedDeclaredType<Q1> type, P p) {
        return QualifiedDeclaredType.<Q2>builder()
                .setJavaType(type.getJavaType())
                .setEnclosingType(visit(type.getEnclosingType(), p))
                .setTypeArguments(visit(type.getTypeArguments(), p))
                .setQualifier(defaultAction(type, p))
                .build();
    }

    @Override
    public QualifiedExecutableType<Q2> visitExecutable(QualifiedExecutableType<Q1> type, P p) {
        return QualifiedExecutableType.<Q2>builder()
                .setJavaType(type.getJavaType())
                .setJavaElement(type.getJavaElement())
                .setReceiverType(visit(type.getReceiverType(), p))
                .setReturnType(visit(type.getReturnType(), p))
                .setParameterTypes(visit(type.getParameterTypes(), p))
                .setThrownTypes(visit(type.getThrownTypes(), p))
                .build();
    }

    @Override
    public QualifiedIntersectionType<Q2> visitIntersection(QualifiedIntersectionType<Q1> type, P p) {
        return QualifiedIntersectionType.<Q2>builder()
                .setJavaType(type.getJavaType())
                .setBounds(visit(type.getBounds(), p))
                .setQualifier(defaultAction(type, p))
                .build();
    }

    @Override
    public QualifiedNoType<Q2> visitNo(QualifiedNoType<Q1> type, P p) {
        return QualifiedNoType.<Q2>builder()
                .setJavaType(type.getJavaType())
                .build();
    }

    @Override
    public QualifiedNullType<Q2> visitNull(QualifiedNullType<Q1> type, P p) {
        return QualifiedNullType.<Q2>builder()
                .setJavaType(type.getJavaType())
                .build();
    }

    @Override
    public QualifiedPrimitiveType<Q2> visitPrimitive(QualifiedPrimitiveType<Q1> type, P p) {
        return QualifiedPrimitiveType.<Q2>builder()
                .setJavaType(type.getJavaType())
                .setQualifier(defaultAction(type, p))
                .build();
    }

    @Override
    public QualifiedTypeVariable<Q2> visitTypeVariable(QualifiedTypeVariable<Q1> type, P p) {
        // TODO(generics): implementation
        throw new UnsupportedOperationException();
        // return QualifiedTypeVariable.<Q2>builder()
        //         .setJavaType(type.getJavaType())
        //         .setLowerBound(visit(type.getLowerBound(), p))
        //         .setUpperBound(visit(type.getUpperBound(), p))
        //         .setQualifier(defaultAction(type, p))
        //         .build();
    }

    @Override
    public QualifiedUnionType<Q2> visitUnion(QualifiedUnionType<Q1> type, P p) {
        return QualifiedUnionType.<Q2>builder()
                .setJavaType(type.getJavaType())
                .setAlternatives(visit(type.getAlternatives(), p))
                .setQualifier(defaultAction(type, p))
                .build();
    }

    @Override
    public QualifiedWildcardType<Q2> visitWildcard(QualifiedWildcardType<Q1> type, P p) {
        // TODO(generics): implementation
        throw new UnsupportedOperationException();
        // return QualifiedWildcardType.<Q2>builder()
        //         .setJavaType(type.getJavaType())
        //         .setExtendsBound(visit(type.getExtendsBound(), p))
        //         .setSuperBound(visit(type.getSuperBound(), p))
        //         .setQualifier(defaultAction(type, p))
        //         .build();
    }
}
