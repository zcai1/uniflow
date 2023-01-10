package org.cfginference.util;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.UnknownTypeException;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor14;
import java.util.List;

// NOTE: The default implementation doesn't stop for cyclic references.
public abstract class TypeScanner<R, P> extends SimpleTypeVisitor14<R, P> {

    protected R scan(TypeMirror type, P p) {
        return (type == null) ? null : type.accept(this, p);
    }

    protected R scan(List<? extends TypeMirror> types, P p) {
        R r = null;
        if (types != null) {
            boolean first = true;
            for (TypeMirror type : types) {
                r = (first ? scan(type, p) : scanAndReduce(type, p, r));
                first = false;
            }
        }
        return r;
    }

    private R scanAndReduce(TypeMirror type, P p, R r) {
        return reduce(scan(type, p), r);
    }

    private R scanAndReduce(List<? extends TypeMirror> types, P p, R r) {
        return reduce(scan(types, p), r);
    }

    public R reduce(R r1, R r2) {
        return r1;
    }

    @Override
    public R visitArray(ArrayType type, P p) {
        return scan(type.getComponentType(), p);
    }

    @Override
    public R visitDeclared(DeclaredType type, P p) {
        return scan(type.getTypeArguments(), p);
    }

    @Override
    public R visitTypeVariable(TypeVariable type, P p) {
        R r = scan(type.getLowerBound(), p);
        return scanAndReduce(type.getUpperBound(), p, r);
    }

    @Override
    public R visitWildcard(WildcardType type, P p) {
        TypeMirror superBound = type.getSuperBound();
        if (superBound != null) {
            return scan(superBound, p);
        }

        TypeMirror extendsBound = type.getExtendsBound();
        if (extendsBound != null) {
            return scan(extendsBound,  p);
        }
        return defaultAction(type, p);
    }

    @Override
    public R visitExecutable(ExecutableType type, P p) {
        R r = scan(type.getTypeVariables(), p);
        r = scanAndReduce(type.getReturnType(), p, r);
        r = scanAndReduce(type.getParameterTypes(), p, r);
        r = scanAndReduce(type.getReceiverType(), p, r);
        r = scanAndReduce(type.getThrownTypes(), p, r);
        return r;
    }

    @Override
    public R visitUnion(UnionType type, P p) {
        return scan(type.getAlternatives(), p);
    }

    @Override
    public R visitIntersection(IntersectionType type, P p) {
        return scan(type.getBounds(), p);
    }

    @Override
    public R visitError(ErrorType type, P p) {
        throw new UnknownTypeException(type, p);
    }
}
