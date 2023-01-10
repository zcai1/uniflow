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

public class QualifiedTypeComboVisitor<Q1 extends Qualifier, Q2 extends Qualifier, R> 
        extends SimpleQualifiedTypeVisitor<Q1, R, QualifiedType<Q2>> {

    @Override
    public final R visitArray(QualifiedArrayType<Q1> type1, QualifiedType<Q2> type2) {
        if (type2 instanceof QualifiedArrayType<Q2> arrayType) {
            return visitArray_Array(type1, arrayType);
        } else if (type2 instanceof QualifiedDeclaredType<Q2> declaredType) {
            return visitArray_Declared(type1, declaredType);
        } else if (type2 instanceof QualifiedExecutableType<Q2> executableType) {
            return visitArray_Executable(type1, executableType);
        } else if (type2 instanceof QualifiedIntersectionType<Q2> intersectionType) {
            return visitArray_Intersection(type1, intersectionType);
        } else if (type2 instanceof QualifiedNoType<Q2> noType) {
            return visitArray_No(type1, noType);
        } else if (type2 instanceof QualifiedNullType<Q2> nullType) {
            return visitArray_Null(type1, nullType);
        } else if (type2 instanceof QualifiedPrimitiveType<Q2> primitiveType) {
            return visitArray_Primitive(type1, primitiveType);
        } else if (type2 instanceof QualifiedTypeVariable<Q2> typeVariable) {
            return visitArray_TypeVariable(type1, typeVariable);
        } else if (type2 instanceof QualifiedUnionType<Q2> unionType) {
            return visitArray_Union(type1, unionType);
        } else if (type2 instanceof QualifiedWildcardType<Q2> wildcardType) {
            return visitArray_Wildcard(type1, wildcardType);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public final R visitDeclared(QualifiedDeclaredType<Q1> type1, QualifiedType<Q2> type2) {
        if (type2 instanceof QualifiedArrayType<Q2> arrayType) {
            return visitDeclared_Array(type1, arrayType);
        } else if (type2 instanceof QualifiedDeclaredType<Q2> declaredType) {
            return visitDeclared_Declared(type1, declaredType);
        } else if (type2 instanceof QualifiedExecutableType<Q2> executableType) {
            return visitDeclared_Executable(type1, executableType);
        } else if (type2 instanceof QualifiedIntersectionType<Q2> intersectionType) {
            return visitDeclared_Intersection(type1, intersectionType);
        } else if (type2 instanceof QualifiedNoType<Q2> noType) {
            return visitDeclared_No(type1, noType);
        } else if (type2 instanceof QualifiedNullType<Q2> nullType) {
            return visitDeclared_Null(type1, nullType);
        } else if (type2 instanceof QualifiedPrimitiveType<Q2> primitiveType) {
            return visitDeclared_Primitive(type1, primitiveType);
        } else if (type2 instanceof QualifiedTypeVariable<Q2> typeVariable) {
            return visitDeclared_TypeVariable(type1, typeVariable);
        } else if (type2 instanceof QualifiedUnionType<Q2> unionType) {
            return visitDeclared_Union(type1, unionType);
        } else if (type2 instanceof QualifiedWildcardType<Q2> wildcardType) {
            return visitDeclared_Wildcard(type1, wildcardType);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public final R visitExecutable(QualifiedExecutableType<Q1> type1, QualifiedType<Q2> type2) {
        if (type2 instanceof QualifiedArrayType<Q2> arrayType) {
            return visitExecutable_Array(type1, arrayType);
        } else if (type2 instanceof QualifiedDeclaredType<Q2> declaredType) {
            return visitExecutable_Declared(type1, declaredType);
        } else if (type2 instanceof QualifiedExecutableType<Q2> executableType) {
            return visitExecutable_Executable(type1, executableType);
        } else if (type2 instanceof QualifiedIntersectionType<Q2> intersectionType) {
            return visitExecutable_Intersection(type1, intersectionType);
        } else if (type2 instanceof QualifiedNoType<Q2> noType) {
            return visitExecutable_No(type1, noType);
        } else if (type2 instanceof QualifiedNullType<Q2> nullType) {
            return visitExecutable_Null(type1, nullType);
        } else if (type2 instanceof QualifiedPrimitiveType<Q2> primitiveType) {
            return visitExecutable_Primitive(type1, primitiveType);
        } else if (type2 instanceof QualifiedTypeVariable<Q2> typeVariable) {
            return visitExecutable_TypeVariable(type1, typeVariable);
        } else if (type2 instanceof QualifiedUnionType<Q2> unionType) {
            return visitExecutable_Union(type1, unionType);
        } else if (type2 instanceof QualifiedWildcardType<Q2> wildcardType) {
            return visitExecutable_Wildcard(type1, wildcardType);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public final R visitIntersection(QualifiedIntersectionType<Q1> type1, QualifiedType<Q2> type2) {
        if (type2 instanceof QualifiedArrayType<Q2> arrayType) {
            return visitIntersection_Array(type1, arrayType);
        } else if (type2 instanceof QualifiedDeclaredType<Q2> declaredType) {
            return visitIntersection_Declared(type1, declaredType);
        } else if (type2 instanceof QualifiedExecutableType<Q2> executableType) {
            return visitIntersection_Executable(type1, executableType);
        } else if (type2 instanceof QualifiedIntersectionType<Q2> intersectionType) {
            return visitIntersection_Intersection(type1, intersectionType);
        } else if (type2 instanceof QualifiedNoType<Q2> noType) {
            return visitIntersection_No(type1, noType);
        } else if (type2 instanceof QualifiedNullType<Q2> nullType) {
            return visitIntersection_Null(type1, nullType);
        } else if (type2 instanceof QualifiedPrimitiveType<Q2> primitiveType) {
            return visitIntersection_Primitive(type1, primitiveType);
        } else if (type2 instanceof QualifiedTypeVariable<Q2> typeVariable) {
            return visitIntersection_TypeVariable(type1, typeVariable);
        } else if (type2 instanceof QualifiedUnionType<Q2> unionType) {
            return visitIntersection_Union(type1, unionType);
        } else if (type2 instanceof QualifiedWildcardType<Q2> wildcardType) {
            return visitIntersection_Wildcard(type1, wildcardType);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public final R visitNo(QualifiedNoType<Q1> type1, QualifiedType<Q2> type2) {
        if (type2 instanceof QualifiedArrayType<Q2> arrayType) {
            return visitNo_Array(type1, arrayType);
        } else if (type2 instanceof QualifiedDeclaredType<Q2> declaredType) {
            return visitNo_Declared(type1, declaredType);
        } else if (type2 instanceof QualifiedExecutableType<Q2> executableType) {
            return visitNo_Executable(type1, executableType);
        } else if (type2 instanceof QualifiedIntersectionType<Q2> intersectionType) {
            return visitNo_Intersection(type1, intersectionType);
        } else if (type2 instanceof QualifiedNoType<Q2> noType) {
            return visitNo_No(type1, noType);
        } else if (type2 instanceof QualifiedNullType<Q2> nullType) {
            return visitNo_Null(type1, nullType);
        } else if (type2 instanceof QualifiedPrimitiveType<Q2> primitiveType) {
            return visitNo_Primitive(type1, primitiveType);
        } else if (type2 instanceof QualifiedTypeVariable<Q2> typeVariable) {
            return visitNo_TypeVariable(type1, typeVariable);
        } else if (type2 instanceof QualifiedUnionType<Q2> unionType) {
            return visitNo_Union(type1, unionType);
        } else if (type2 instanceof QualifiedWildcardType<Q2> wildcardType) {
            return visitNo_Wildcard(type1, wildcardType);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public final R visitNull(QualifiedNullType<Q1> type1, QualifiedType<Q2> type2) {
        if (type2 instanceof QualifiedArrayType<Q2> arrayType) {
            return visitNull_Array(type1, arrayType);
        } else if (type2 instanceof QualifiedDeclaredType<Q2> declaredType) {
            return visitNull_Declared(type1, declaredType);
        } else if (type2 instanceof QualifiedExecutableType<Q2> executableType) {
            return visitNull_Executable(type1, executableType);
        } else if (type2 instanceof QualifiedIntersectionType<Q2> intersectionType) {
            return visitNull_Intersection(type1, intersectionType);
        } else if (type2 instanceof QualifiedNoType<Q2> noType) {
            return visitNull_No(type1, noType);
        } else if (type2 instanceof QualifiedNullType<Q2> nullType) {
            return visitNull_Null(type1, nullType);
        } else if (type2 instanceof QualifiedPrimitiveType<Q2> primitiveType) {
            return visitNull_Primitive(type1, primitiveType);
        } else if (type2 instanceof QualifiedTypeVariable<Q2> typeVariable) {
            return visitNull_TypeVariable(type1, typeVariable);
        } else if (type2 instanceof QualifiedUnionType<Q2> unionType) {
            return visitNull_Union(type1, unionType);
        } else if (type2 instanceof QualifiedWildcardType<Q2> wildcardType) {
            return visitNull_Wildcard(type1, wildcardType);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public final R visitPrimitive(QualifiedPrimitiveType<Q1> type1, QualifiedType<Q2> type2) {
        if (type2 instanceof QualifiedArrayType<Q2> arrayType) {
            return visitPrimitive_Array(type1, arrayType);
        } else if (type2 instanceof QualifiedDeclaredType<Q2> declaredType) {
            return visitPrimitive_Declared(type1, declaredType);
        } else if (type2 instanceof QualifiedExecutableType<Q2> executableType) {
            return visitPrimitive_Executable(type1, executableType);
        } else if (type2 instanceof QualifiedIntersectionType<Q2> intersectionType) {
            return visitPrimitive_Intersection(type1, intersectionType);
        } else if (type2 instanceof QualifiedNoType<Q2> noType) {
            return visitPrimitive_No(type1, noType);
        } else if (type2 instanceof QualifiedNullType<Q2> nullType) {
            return visitPrimitive_Null(type1, nullType);
        } else if (type2 instanceof QualifiedPrimitiveType<Q2> primitiveType) {
            return visitPrimitive_Primitive(type1, primitiveType);
        } else if (type2 instanceof QualifiedTypeVariable<Q2> typeVariable) {
            return visitPrimitive_TypeVariable(type1, typeVariable);
        } else if (type2 instanceof QualifiedUnionType<Q2> unionType) {
            return visitPrimitive_Union(type1, unionType);
        } else if (type2 instanceof QualifiedWildcardType<Q2> wildcardType) {
            return visitPrimitive_Wildcard(type1, wildcardType);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public final R visitTypeVariable(QualifiedTypeVariable<Q1> type1, QualifiedType<Q2> type2) {
        if (type2 instanceof QualifiedArrayType<Q2> arrayType) {
            return visitTypeVariable_Array(type1, arrayType);
        } else if (type2 instanceof QualifiedDeclaredType<Q2> declaredType) {
            return visitTypeVariable_Declared(type1, declaredType);
        } else if (type2 instanceof QualifiedExecutableType<Q2> executableType) {
            return visitTypeVariable_Executable(type1, executableType);
        } else if (type2 instanceof QualifiedIntersectionType<Q2> intersectionType) {
            return visitTypeVariable_Intersection(type1, intersectionType);
        } else if (type2 instanceof QualifiedNoType<Q2> noType) {
            return visitTypeVariable_No(type1, noType);
        } else if (type2 instanceof QualifiedNullType<Q2> nullType) {
            return visitTypeVariable_Null(type1, nullType);
        } else if (type2 instanceof QualifiedPrimitiveType<Q2> primitiveType) {
            return visitTypeVariable_Primitive(type1, primitiveType);
        } else if (type2 instanceof QualifiedTypeVariable<Q2> typeVariable) {
            return visitTypeVariable_TypeVariable(type1, typeVariable);
        } else if (type2 instanceof QualifiedUnionType<Q2> unionType) {
            return visitTypeVariable_Union(type1, unionType);
        } else if (type2 instanceof QualifiedWildcardType<Q2> wildcardType) {
            return visitTypeVariable_Wildcard(type1, wildcardType);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public final R visitUnion(QualifiedUnionType<Q1> type1, QualifiedType<Q2> type2) {
        if (type2 instanceof QualifiedArrayType<Q2> arrayType) {
            return visitUnion_Array(type1, arrayType);
        } else if (type2 instanceof QualifiedDeclaredType<Q2> declaredType) {
            return visitUnion_Declared(type1, declaredType);
        } else if (type2 instanceof QualifiedExecutableType<Q2> executableType) {
            return visitUnion_Executable(type1, executableType);
        } else if (type2 instanceof QualifiedIntersectionType<Q2> intersectionType) {
            return visitUnion_Intersection(type1, intersectionType);
        } else if (type2 instanceof QualifiedNoType<Q2> noType) {
            return visitUnion_No(type1, noType);
        } else if (type2 instanceof QualifiedNullType<Q2> nullType) {
            return visitUnion_Null(type1, nullType);
        } else if (type2 instanceof QualifiedPrimitiveType<Q2> primitiveType) {
            return visitUnion_Primitive(type1, primitiveType);
        } else if (type2 instanceof QualifiedTypeVariable<Q2> typeVariable) {
            return visitUnion_TypeVariable(type1, typeVariable);
        } else if (type2 instanceof QualifiedUnionType<Q2> unionType) {
            return visitUnion_Union(type1, unionType);
        } else if (type2 instanceof QualifiedWildcardType<Q2> wildcardType) {
            return visitUnion_Wildcard(type1, wildcardType);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public final R visitWildcard(QualifiedWildcardType<Q1> type1, QualifiedType<Q2> type2) {
        if (type2 instanceof QualifiedArrayType<Q2> arrayType) {
            return visitWildcard_Array(type1, arrayType);
        } else if (type2 instanceof QualifiedDeclaredType<Q2> declaredType) {
            return visitWildcard_Declared(type1, declaredType);
        } else if (type2 instanceof QualifiedExecutableType<Q2> executableType) {
            return visitWildcard_Executable(type1, executableType);
        } else if (type2 instanceof QualifiedIntersectionType<Q2> intersectionType) {
            return visitWildcard_Intersection(type1, intersectionType);
        } else if (type2 instanceof QualifiedNoType<Q2> noType) {
            return visitWildcard_No(type1, noType);
        } else if (type2 instanceof QualifiedNullType<Q2> nullType) {
            return visitWildcard_Null(type1, nullType);
        } else if (type2 instanceof QualifiedPrimitiveType<Q2> primitiveType) {
            return visitWildcard_Primitive(type1, primitiveType);
        } else if (type2 instanceof QualifiedTypeVariable<Q2> typeVariable) {
            return visitWildcard_TypeVariable(type1, typeVariable);
        } else if (type2 instanceof QualifiedUnionType<Q2> unionType) {
            return visitWildcard_Union(type1, unionType);
        } else if (type2 instanceof QualifiedWildcardType<Q2> wildcardType) {
            return visitWildcard_Wildcard(type1, wildcardType);
        }
        throw new IllegalArgumentException();
    }

    protected R visitArray_Array(QualifiedArrayType<Q1> type1, QualifiedArrayType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitArray_Declared(QualifiedArrayType<Q1> type1, QualifiedDeclaredType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitArray_No(QualifiedArrayType<Q1> type1, QualifiedNoType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitArray_Null(QualifiedArrayType<Q1> type1, QualifiedNullType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitArray_Executable(QualifiedArrayType<Q1> type1, QualifiedExecutableType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitArray_Intersection(QualifiedArrayType<Q1> type1, QualifiedIntersectionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitArray_Primitive(QualifiedArrayType<Q1> type1, QualifiedPrimitiveType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitArray_TypeVariable(QualifiedArrayType<Q1> type1, QualifiedTypeVariable<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitArray_Union(QualifiedArrayType<Q1> type1, QualifiedUnionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitArray_Wildcard(QualifiedArrayType<Q1> type1, QualifiedWildcardType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitDeclared_Array(QualifiedDeclaredType<Q1> type1, QualifiedArrayType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitDeclared_Declared(QualifiedDeclaredType<Q1> type1, QualifiedDeclaredType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitDeclared_No(QualifiedDeclaredType<Q1> type1, QualifiedNoType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitDeclared_Null(QualifiedDeclaredType<Q1> type1, QualifiedNullType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitDeclared_Executable(QualifiedDeclaredType<Q1> type1, QualifiedExecutableType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitDeclared_Intersection(QualifiedDeclaredType<Q1> type1, QualifiedIntersectionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitDeclared_Primitive(QualifiedDeclaredType<Q1> type1, QualifiedPrimitiveType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitDeclared_TypeVariable(QualifiedDeclaredType<Q1> type1, QualifiedTypeVariable<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitDeclared_Union(QualifiedDeclaredType<Q1> type1, QualifiedUnionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitDeclared_Wildcard(QualifiedDeclaredType<Q1> type1, QualifiedWildcardType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNo_Array(QualifiedNoType<Q1> type1, QualifiedArrayType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNo_Declared(QualifiedNoType<Q1> type1, QualifiedDeclaredType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNo_No(QualifiedNoType<Q1> type1, QualifiedNoType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNo_Null(QualifiedNoType<Q1> type1, QualifiedNullType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNo_Executable(QualifiedNoType<Q1> type1, QualifiedExecutableType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNo_Intersection(QualifiedNoType<Q1> type1, QualifiedIntersectionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNo_Primitive(QualifiedNoType<Q1> type1, QualifiedPrimitiveType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNo_TypeVariable(QualifiedNoType<Q1> type1, QualifiedTypeVariable<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNo_Union(QualifiedNoType<Q1> type1, QualifiedUnionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNo_Wildcard(QualifiedNoType<Q1> type1, QualifiedWildcardType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNull_Array(QualifiedNullType<Q1> type1, QualifiedArrayType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNull_Declared(QualifiedNullType<Q1> type1, QualifiedDeclaredType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNull_No(QualifiedNullType<Q1> type1, QualifiedNoType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNull_Null(QualifiedNullType<Q1> type1, QualifiedNullType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNull_Executable(QualifiedNullType<Q1> type1, QualifiedExecutableType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNull_Intersection(QualifiedNullType<Q1> type1, QualifiedIntersectionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNull_Primitive(QualifiedNullType<Q1> type1, QualifiedPrimitiveType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNull_TypeVariable(QualifiedNullType<Q1> type1, QualifiedTypeVariable<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNull_Union(QualifiedNullType<Q1> type1, QualifiedUnionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitNull_Wildcard(QualifiedNullType<Q1> type1, QualifiedWildcardType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitExecutable_Array(QualifiedExecutableType<Q1> type1, QualifiedArrayType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitExecutable_Declared(QualifiedExecutableType<Q1> type1, QualifiedDeclaredType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitExecutable_No(QualifiedExecutableType<Q1> type1, QualifiedNoType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitExecutable_Null(QualifiedExecutableType<Q1> type1, QualifiedNullType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitExecutable_Executable(QualifiedExecutableType<Q1> type1, QualifiedExecutableType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitExecutable_Intersection(QualifiedExecutableType<Q1> type1, QualifiedIntersectionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitExecutable_Primitive(QualifiedExecutableType<Q1> type1, QualifiedPrimitiveType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitExecutable_TypeVariable(QualifiedExecutableType<Q1> type1, QualifiedTypeVariable<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitExecutable_Union(QualifiedExecutableType<Q1> type1, QualifiedUnionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitExecutable_Wildcard(QualifiedExecutableType<Q1> type1, QualifiedWildcardType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitIntersection_Array(QualifiedIntersectionType<Q1> type1, QualifiedArrayType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitIntersection_Declared(QualifiedIntersectionType<Q1> type1, QualifiedDeclaredType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitIntersection_No(QualifiedIntersectionType<Q1> type1, QualifiedNoType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitIntersection_Null(QualifiedIntersectionType<Q1> type1, QualifiedNullType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitIntersection_Executable(QualifiedIntersectionType<Q1> type1, QualifiedExecutableType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitIntersection_Intersection(QualifiedIntersectionType<Q1> type1, QualifiedIntersectionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitIntersection_Primitive(QualifiedIntersectionType<Q1> type1, QualifiedPrimitiveType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitIntersection_TypeVariable(QualifiedIntersectionType<Q1> type1, QualifiedTypeVariable<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitIntersection_Union(QualifiedIntersectionType<Q1> type1, QualifiedUnionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitIntersection_Wildcard(QualifiedIntersectionType<Q1> type1, QualifiedWildcardType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitPrimitive_Array(QualifiedPrimitiveType<Q1> type1, QualifiedArrayType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitPrimitive_Declared(QualifiedPrimitiveType<Q1> type1, QualifiedDeclaredType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitPrimitive_No(QualifiedPrimitiveType<Q1> type1, QualifiedNoType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitPrimitive_Null(QualifiedPrimitiveType<Q1> type1, QualifiedNullType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitPrimitive_Executable(QualifiedPrimitiveType<Q1> type1, QualifiedExecutableType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitPrimitive_Intersection(QualifiedPrimitiveType<Q1> type1, QualifiedIntersectionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitPrimitive_Primitive(QualifiedPrimitiveType<Q1> type1, QualifiedPrimitiveType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitPrimitive_TypeVariable(QualifiedPrimitiveType<Q1> type1, QualifiedTypeVariable<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitPrimitive_Union(QualifiedPrimitiveType<Q1> type1, QualifiedUnionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitPrimitive_Wildcard(QualifiedPrimitiveType<Q1> type1, QualifiedWildcardType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitTypeVariable_Array(QualifiedTypeVariable<Q1> type1, QualifiedArrayType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitTypeVariable_Declared(QualifiedTypeVariable<Q1> type1, QualifiedDeclaredType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitTypeVariable_No(QualifiedTypeVariable<Q1> type1, QualifiedNoType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitTypeVariable_Null(QualifiedTypeVariable<Q1> type1, QualifiedNullType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitTypeVariable_Executable(QualifiedTypeVariable<Q1> type1, QualifiedExecutableType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitTypeVariable_Intersection(QualifiedTypeVariable<Q1> type1, QualifiedIntersectionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitTypeVariable_Primitive(QualifiedTypeVariable<Q1> type1, QualifiedPrimitiveType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitTypeVariable_TypeVariable(QualifiedTypeVariable<Q1> type1, QualifiedTypeVariable<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitTypeVariable_Union(QualifiedTypeVariable<Q1> type1, QualifiedUnionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitTypeVariable_Wildcard(QualifiedTypeVariable<Q1> type1, QualifiedWildcardType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitUnion_Array(QualifiedUnionType<Q1> type1, QualifiedArrayType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitUnion_Declared(QualifiedUnionType<Q1> type1, QualifiedDeclaredType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitUnion_No(QualifiedUnionType<Q1> type1, QualifiedNoType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitUnion_Null(QualifiedUnionType<Q1> type1, QualifiedNullType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitUnion_Executable(QualifiedUnionType<Q1> type1, QualifiedExecutableType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitUnion_Intersection(QualifiedUnionType<Q1> type1, QualifiedIntersectionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitUnion_Primitive(QualifiedUnionType<Q1> type1, QualifiedPrimitiveType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitUnion_TypeVariable(QualifiedUnionType<Q1> type1, QualifiedTypeVariable<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitUnion_Union(QualifiedUnionType<Q1> type1, QualifiedUnionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitUnion_Wildcard(QualifiedUnionType<Q1> type1, QualifiedWildcardType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitWildcard_Array(QualifiedWildcardType<Q1> type1, QualifiedArrayType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitWildcard_Declared(QualifiedWildcardType<Q1> type1, QualifiedDeclaredType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitWildcard_No(QualifiedWildcardType<Q1> type1, QualifiedNoType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitWildcard_Null(QualifiedWildcardType<Q1> type1, QualifiedNullType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitWildcard_Executable(QualifiedWildcardType<Q1> type1, QualifiedExecutableType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitWildcard_Intersection(QualifiedWildcardType<Q1> type1, QualifiedIntersectionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitWildcard_Primitive(QualifiedWildcardType<Q1> type1, QualifiedPrimitiveType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitWildcard_TypeVariable(QualifiedWildcardType<Q1> type1, QualifiedTypeVariable<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitWildcard_Union(QualifiedWildcardType<Q1> type1, QualifiedUnionType<Q2> type2) {
        return defaultAction(type1, type2);
    }

    protected R visitWildcard_Wildcard(QualifiedWildcardType<Q1> type1, QualifiedWildcardType<Q2> type2) {
        return defaultAction(type1, type2);
    }
}
