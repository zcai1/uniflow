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

import javax.lang.model.util.Types;

// doesn't work, we still want qualifier hierarchy to determine whether two types match
public class QualifiedTypesRelation {

    private QualifiedTypesRelation() {}

    private static DoubleQualifiedTypeScanner<?, ?, Boolean> haveSameQualifiers = new DoubleQualifiedTypeScanner<>() {
        @Override
        protected Boolean defaultAction(QualifiedType<Qualifier> type1, QualifiedType<Qualifier> type2) {
            return true;
        }

        @Override
        public Boolean reduce(Boolean r1, Boolean r2) {
            return r1 && r2;
        }

        @Override
        public Boolean visitArray(QualifiedArrayType<Qualifier> type1, QualifiedArrayType<Qualifier> type2) {
            return type1.getQualifier().equals(type2) && super.visitArray(type1, type2);
        }

        @Override
        public Boolean visitDeclared(QualifiedDeclaredType<Qualifier> type1, QualifiedDeclaredType<Qualifier> type2) {
            return super.visitDeclared(type1, type2);
        }

        @Override
        public Boolean visitExecutable(QualifiedExecutableType<Qualifier> type1, QualifiedExecutableType<Qualifier> type2) {
            return super.visitExecutable(type1, type2);
        }

        @Override
        public Boolean visitIntersection(QualifiedIntersectionType<Qualifier> type1, QualifiedIntersectionType<Qualifier> type2) {
            return super.visitIntersection(type1, type2);
        }

        @Override
        public Boolean visitNo(QualifiedNoType<Qualifier> type1, QualifiedNoType<Qualifier> type2) {
            return super.visitNo(type1, type2);
        }

        @Override
        public Boolean visitNull(QualifiedNullType<Qualifier> type1, QualifiedNullType<Qualifier> type2) {
            return super.visitNull(type1, type2);
        }

        @Override
        public Boolean visitPrimitive(QualifiedPrimitiveType<Qualifier> type1, QualifiedPrimitiveType<Qualifier> type2) {
            return super.visitPrimitive(type1, type2);
        }

        @Override
        public Boolean visitUnion(QualifiedUnionType<Qualifier> type1, QualifiedUnionType<Qualifier> type2) {
            return super.visitUnion(type1, type2);
        }
    };

    public static boolean isSameType(Types types, QualifiedType<?> t1, QualifiedType<?> t2) {
        if (t1.getJavaType() != t2.getJavaType() && !types.isSameType(t1.getJavaType(), t2.getJavaType())) {
            return false;
        }
        return false;
    }
}
