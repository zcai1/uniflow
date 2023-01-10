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

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

public abstract class QualifiedTypeBuilder<Q extends Qualifier, P> implements TypeVisitor<QualifiedType<Q>, P> {

    protected abstract Q getQualifier(TypeMirror t, P p);

    @Override
    public QualifiedType<Q> visit(TypeMirror t, P p) {
        return t.accept(this, p);
    }

    @Override
    public QualifiedIntersectionType<Q> visitIntersection(IntersectionType t, P p) {
        return defaultBuilder(t, p).build();
    }

    protected QualifiedIntersectionType.Builder<Q> defaultBuilder(IntersectionType t, P p) {
        return QualifiedIntersectionType.<Q>builder()
                .setJavaType(t)
                .setBounds(t.getBounds().stream().map(b -> visit(b, p)).toList())
                .setQualifier(getQualifier(t, p));
    }

    @Override
    public QualifiedUnionType<Q> visitUnion(UnionType t, P p) {
        return defaultBuilder(t, p).build();
    }

    protected QualifiedUnionType.Builder<Q> defaultBuilder(UnionType t, P p) {
        return QualifiedUnionType.<Q>builder()
                .setJavaType(t)
                .setAlternatives(t.getAlternatives().stream().map(a -> visit(a, p)).toList())
                .setQualifier(getQualifier(t, p));
    }

    @Override
    public QualifiedPrimitiveType<Q> visitPrimitive(PrimitiveType t, P p) {
        return defaultBuilder(t, p).build();
    }

    protected QualifiedPrimitiveType.Builder<Q> defaultBuilder(PrimitiveType t, P p) {
        return QualifiedPrimitiveType.<Q>builder()
                .setJavaType(t)
                .setQualifier(getQualifier(t, p));
    }

    @Override
    public QualifiedNullType<Q> visitNull(NullType t, P p) {
        return defaultBuilder(t, p).build();
    }

    protected QualifiedNullType.Builder<Q> defaultBuilder(NullType t, P p) {
        return QualifiedNullType.<Q>builder()
                .setJavaType(t)
                .setQualifier(getQualifier(t, p));
    }

    @Override
    public QualifiedArrayType<Q> visitArray(ArrayType t, P p) {
        return defaultBuilder(t, p).build();
    }

    protected QualifiedArrayType.Builder<Q> defaultBuilder(ArrayType t, P p) {
        return QualifiedArrayType.<Q>builder()
                .setJavaType(t)
                .setComponentType(visit(t.getComponentType(), p))
                .setQualifier(getQualifier(t, p));
    }

    @Override
    public QualifiedDeclaredType<Q> visitDeclared(DeclaredType t, P p) {
        return defaultBuilder(t, p).build();
    }

    protected QualifiedDeclaredType.Builder<Q> defaultBuilder(DeclaredType t, P p) {
        if (t.getTypeArguments().size() > 0) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException("Qualified %s is not supported".formatted(t));
        }

        return QualifiedDeclaredType.<Q>builder()
                .setJavaType(t)
                .setEnclosingType(visit(t.getEnclosingType(), p))
                .setTypeArguments(t.getTypeArguments().stream().map(ta -> visit(ta, p)).toList())
                .setQualifier(getQualifier(t, p));
    }

    @Override
    public QualifiedExecutableType<Q> visitExecutable(ExecutableType t, P p) {
        return defaultBuilder(t, p).build();
    }

    protected QualifiedExecutableType.Builder<Q> defaultBuilder(ExecutableType t, P p) {
        return QualifiedExecutableType.<Q>builder()
                .setJavaType(t)
                .setReturnType(visit(t.getReturnType(), p))
                .setParameterTypes(t.getParameterTypes().stream().map(param -> visit(param, p)).toList())
                .setReceiverType(visit(t.getReturnType(), p))
                .setThrownTypes(t.getThrownTypes().stream().map(tt -> visit(tt, p)).toList());
    }

    @Override
    public QualifiedNoType<Q> visitNoType(NoType t, P p) {
        return defaultBuilder(t, p).build();
    }

    protected QualifiedNoType.Builder<Q> defaultBuilder(NoType t, P p) {
        return QualifiedNoType.<Q>builder()
                .setJavaType(t);
    }

    @Override
    public QualifiedType<Q> visitError(ErrorType t, P p) {
        throw new UnsupportedOperationException("Qualified %s is not supported".formatted(t));
    }

    @Override
    public QualifiedTypeVariable<Q> visitTypeVariable(TypeVariable t, P p) {
        // TODO(generics): implementation
        throw new UnsupportedOperationException("Qualified %s is not supported".formatted(t));
    }

    @Override
    public QualifiedWildcardType<Q> visitWildcard(WildcardType t, P p) {
        // TODO(generics): implementation
        throw new UnsupportedOperationException("Qualified %s is not supported".formatted(t));
    }

    @Override
    public QualifiedType<Q> visitUnknown(TypeMirror t, P p) {
        throw new UnsupportedOperationException("Qualified %s is not supported".formatted(t));
    }
}
