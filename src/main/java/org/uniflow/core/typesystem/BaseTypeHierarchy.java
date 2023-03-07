package org.uniflow.core.typesystem;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import org.uniflow.core.PluginOptions;
import org.uniflow.core.flow.SlotQualifierHierarchy;
import org.uniflow.core.model.constraint.AlwaysFalseConstraint;
import org.uniflow.core.model.constraint.Constraint;
import org.uniflow.core.model.constraint.ConstraintManager;
import org.uniflow.core.model.reporting.PluginError;
import org.uniflow.core.model.slot.ProductSlot;
import org.uniflow.core.model.type.PrimaryQualifiedType;
import org.uniflow.core.model.type.QualifiedArrayType;
import org.uniflow.core.model.type.QualifiedDeclaredType;
import org.uniflow.core.model.type.QualifiedIntersectionType;
import org.uniflow.core.model.type.QualifiedNullType;
import org.uniflow.core.model.type.QualifiedPrimitiveType;
import org.uniflow.core.model.type.QualifiedType;
import org.uniflow.core.model.type.QualifiedTypeVariable;
import org.uniflow.core.model.type.QualifiedUnionType;
import org.uniflow.core.model.type.QualifiedWildcardType;
import org.uniflow.core.model.util.DoubleQualifiedTypeScanner;
import org.uniflow.core.model.util.QualifiedTypeComboVisitor;
import org.checkerframework.javacutil.TypesUtils;

import javax.lang.model.util.Types;

public class BaseTypeHierarchy implements TypeHierarchy {

    protected final PluginOptions options;

    protected final SlotQualifierHierarchy slotQualifierHierarchy;

    protected final Types types;

    protected final ConstraintManager constraintManager;

    protected final ImmutableSet<QualifierHierarchy> qualifierHierarchies;

    public BaseTypeHierarchy(Context context, TypeSystem typeSystem) {
        this.options = PluginOptions.instance(context);
        this.slotQualifierHierarchy = SlotQualifierHierarchy.instance(context);
        this.types = JavacTypes.instance(context);
        this.constraintManager = ConstraintManager.instance(context);
        this.qualifierHierarchies = ImmutableSet.copyOf(typeSystem.getQualifierHierarchies());
    }

    @Override
    public SetMultimap<QualifierHierarchy, Constraint> getSubtypeConstraints(QualifiedType<ProductSlot> subType,
                                                                             QualifiedType<ProductSlot> superType) {
        SubtypeVisitor visitor = createSubtypeVisitor();
        visitor.visit(subType, superType);
        return Multimaps.unmodifiableSetMultimap(visitor.constraints);
    }

    protected SetMultimap<QualifierHierarchy, Constraint> getEqualityConstraints(QualifiedType<ProductSlot> type1,
                                                                                 QualifiedType<ProductSlot> type2) {
        if (!type1.structurallyEquals(types, type2)) {
            SetMultimap<QualifierHierarchy, Constraint> constraints = LinkedHashMultimap.create();
            for (QualifierHierarchy hierarchy : qualifierHierarchies) {
                constraints.put(hierarchy, AlwaysFalseConstraint.instance());
            }
            return constraints;
        }

        EqualityVisitor visitor = createEqualityVisitor();
        visitor.scan(type1, type2);
        return Multimaps.unmodifiableSetMultimap(visitor.constraints);
    }

    protected SubtypeVisitor createSubtypeVisitor() {
        return new SubtypeVisitor();
    }

    protected EqualityVisitor createEqualityVisitor() {
        return new EqualityVisitor();
    }

    protected class SubtypeVisitor extends QualifiedTypeComboVisitor<ProductSlot, ProductSlot, Void> {

        protected SetMultimap<QualifierHierarchy, Constraint> constraints = LinkedHashMultimap.create();

        @Override
        protected Void defaultAction(QualifiedType<ProductSlot> subType, QualifiedType<ProductSlot> superType) {
            throw new PluginError("Incomparable types, t1=%s, t2=%s", subType, superType);
        }

        private void addConstraints(PrimaryQualifiedType<ProductSlot> subType,
                                    PrimaryQualifiedType<ProductSlot> superType) {
            SetMultimap<QualifierHierarchy, Constraint> subtypeConstraints =
                    slotQualifierHierarchy.getSubtypeConstraints(
                            subType.getQualifier(),
                            superType.getQualifier(),
                            qualifierHierarchies
                    );
            constraints.putAll(subtypeConstraints);
        }

        @Override
        protected Void visitArray_Array(QualifiedArrayType<ProductSlot> subType,
                                        QualifiedArrayType<ProductSlot> superType) {
            addConstraints(subType, superType);

            if (options.isInvariantArrays()) {
                constraints.putAll(getEqualityConstraints(subType.getComponentType(), superType.getComponentType()));
            } else {
                visit(subType.getComponentType(), superType.getComponentType());
            }
            return null;
        }

        @Override
        protected Void visitArray_Declared(QualifiedArrayType<ProductSlot> subType,
                                           QualifiedDeclaredType<ProductSlot> superType) {
            addConstraints(subType, superType);
            return null;
        }

        @Override
        protected Void visitArray_Null(QualifiedArrayType<ProductSlot> subType,
                                       QualifiedNullType<ProductSlot> superType) {
            addConstraints(subType, superType);
            return null;
        }

        @Override
        protected Void visitArray_Intersection(QualifiedArrayType<ProductSlot> subType,
                                               QualifiedIntersectionType<ProductSlot> superType) {
            // TODO: should cast subType to super first, then call visit
            visitType_Intersection(subType, superType);
            return null;
        }

        @Override
        protected Void visitArray_TypeVariable(QualifiedArrayType<ProductSlot> subType,
                                               QualifiedTypeVariable<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitArray_Wildcard(QualifiedArrayType<ProductSlot> subType,
                                           QualifiedWildcardType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitDeclared_Array(QualifiedDeclaredType<ProductSlot> subType,
                                           QualifiedArrayType<ProductSlot> superType) {
            addConstraints(subType, superType);
            return null;
        }

        @Override
        protected Void visitDeclared_Declared(QualifiedDeclaredType<ProductSlot> subType,
                                              QualifiedDeclaredType<ProductSlot> superType) {
            addConstraints(subType, superType);
            // TODO(generics): type arguments (inferred/uninferred)
            return null;
        }

        @Override
        protected Void visitDeclared_Intersection(QualifiedDeclaredType<ProductSlot> subType,
                                                  QualifiedIntersectionType<ProductSlot> superType) {
            visitType_Intersection(subType, superType);
            return null;
        }

        @Override
        protected Void visitDeclared_Null(QualifiedDeclaredType<ProductSlot> subType,
                                          QualifiedNullType<ProductSlot> superType) {
            addConstraints(subType, superType);
            return null;
        }

        @Override
        protected Void visitDeclared_Primitive(QualifiedDeclaredType<ProductSlot> subType,
                                               QualifiedPrimitiveType<ProductSlot> superType) {
            // TODO: should probably cast subType to super first (for unboxing)
            addConstraints(subType, superType);
            return null;
        }

        @Override
        protected Void visitDeclared_Union(QualifiedDeclaredType<ProductSlot> subType,
                                           QualifiedUnionType<ProductSlot> superType) {
            visitType_Union(subType, superType);
            return null;
        }

        @Override
        protected Void visitDeclared_TypeVariable(QualifiedDeclaredType<ProductSlot> subType,
                                                  QualifiedTypeVariable<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitDeclared_Wildcard(QualifiedDeclaredType<ProductSlot> subType,
                                              QualifiedWildcardType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitIntersection_Declared(QualifiedIntersectionType<ProductSlot> subType,
                                                  QualifiedDeclaredType<ProductSlot> superType) {
            visitIntersection_Type(subType, superType);
            return null;
        }

        @Override
        protected Void visitIntersection_Null(QualifiedIntersectionType<ProductSlot> subType,
                                              QualifiedNullType<ProductSlot> superType) {
            // this can occur through capture conversion/comparing bounds
            for (QualifiedType<ProductSlot> subTypeBound : subType.getBounds()) {
                if (subTypeBound instanceof PrimaryQualifiedType<ProductSlot> pqSubTypeBound) {
                    addConstraints(pqSubTypeBound, superType);
                }
            }
            return null;
        }

        @Override
        protected Void visitIntersection_Intersection(QualifiedIntersectionType<ProductSlot> subType,
                                                      QualifiedIntersectionType<ProductSlot> superType) {
            for (QualifiedType<ProductSlot> subTypeBound : subType.getBounds()) {
                visitType_Intersection(subTypeBound, superType);
            }
            return null;
        }

        @Override
        protected Void visitIntersection_Primitive(QualifiedIntersectionType<ProductSlot> subType,
                                                   QualifiedPrimitiveType<ProductSlot> superType) {
            for (QualifiedType<ProductSlot> bound : subType.getBounds()) {
                // Bounds in an intersection type can have at most one class
                if (TypesUtils.isBoxedPrimitive(bound.getJavaType())) {
                    visit(bound, superType);
                }
            }
            return null;
        }

        @Override
        protected Void visitIntersection_TypeVariable(QualifiedIntersectionType<ProductSlot> subType,
                                                      QualifiedTypeVariable<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitIntersection_Wildcard(QualifiedIntersectionType<ProductSlot> subType,
                                                  QualifiedWildcardType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitNull_Array(QualifiedNullType<ProductSlot> subType,
                                       QualifiedArrayType<ProductSlot> superType) {
            addConstraints(subType, superType);
            return null;
        }

        @Override
        protected Void visitNull_Declared(QualifiedNullType<ProductSlot> subType,
                                          QualifiedDeclaredType<ProductSlot> superType) {
            addConstraints(subType, superType);
            return null;
        }

        @Override
        protected Void visitNull_Null(QualifiedNullType<ProductSlot> subType,
                                      QualifiedNullType<ProductSlot> superType) {
            addConstraints(subType, superType);
            return null;
        }

        @Override
        protected Void visitNull_Intersection(QualifiedNullType<ProductSlot> subType,
                                              QualifiedIntersectionType<ProductSlot> superType) {
            visitType_Intersection(subType, superType);
            return null;
        }

        @Override
        protected Void visitNull_Primitive(QualifiedNullType<ProductSlot> subType,
                                           QualifiedPrimitiveType<ProductSlot> superType) {
            addConstraints(subType, superType);
            return null;
        }

        @Override
        protected Void visitNull_TypeVariable(QualifiedNullType<ProductSlot> subType,
                                              QualifiedTypeVariable<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitNull_Union(QualifiedNullType<ProductSlot> subType,
                                       QualifiedUnionType<ProductSlot> superType) {
            return null;
        }

        @Override
        protected Void visitNull_Wildcard(QualifiedNullType<ProductSlot> subType,
                                          QualifiedWildcardType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitPrimitive_Declared(QualifiedPrimitiveType<ProductSlot> subType,
                                               QualifiedDeclaredType<ProductSlot> superType) {
            // TODO: should probably cast subType to super first (for boxing)
            addConstraints(subType, superType);
            return null;
        }

        @Override
        protected Void visitPrimitive_Intersection(QualifiedPrimitiveType<ProductSlot> subType,
                                                   QualifiedIntersectionType<ProductSlot> superType) {
            visitType_Intersection(subType, superType);
            return null;
        }

        @Override
        protected Void visitPrimitive_Primitive(QualifiedPrimitiveType<ProductSlot> subType,
                                                QualifiedPrimitiveType<ProductSlot> superType) {
            addConstraints(subType, superType);
            return null;
        }

        @Override
        protected Void visitPrimitive_TypeVariable(QualifiedPrimitiveType<ProductSlot> subType,
                                                   QualifiedTypeVariable<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitPrimitive_Wildcard(QualifiedPrimitiveType<ProductSlot> subType,
                                               QualifiedWildcardType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitUnion_Declared(QualifiedUnionType<ProductSlot> subType,
                                           QualifiedDeclaredType<ProductSlot> superType) {
            visitUnion_Type(subType, superType);
            return null;
        }

        @Override
        protected Void visitUnion_Intersection(QualifiedUnionType<ProductSlot> subType,
                                               QualifiedIntersectionType<ProductSlot> superType) {
            visitUnion_Type(subType, superType);
            return null;
        }

        @Override
        protected Void visitUnion_TypeVariable(QualifiedUnionType<ProductSlot> subType,
                                               QualifiedTypeVariable<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitUnion_Union(QualifiedUnionType<ProductSlot> subType,
                                        QualifiedUnionType<ProductSlot> superType) {
            visitUnion_Type(subType, superType);
            return null;
        }

        @Override
        protected Void visitUnion_Wildcard(QualifiedUnionType<ProductSlot> subType,
                                           QualifiedWildcardType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitTypeVariable_Array(QualifiedTypeVariable<ProductSlot> subType,
                                               QualifiedArrayType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitTypeVariable_Declared(QualifiedTypeVariable<ProductSlot> subType,
                                                  QualifiedDeclaredType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitTypeVariable_Null(QualifiedTypeVariable<ProductSlot> subType,
                                              QualifiedNullType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitTypeVariable_Intersection(QualifiedTypeVariable<ProductSlot> subType,
                                                      QualifiedIntersectionType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitTypeVariable_Primitive(QualifiedTypeVariable<ProductSlot> subType,
                                                   QualifiedPrimitiveType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitTypeVariable_TypeVariable(QualifiedTypeVariable<ProductSlot> subType,
                                                      QualifiedTypeVariable<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitTypeVariable_Wildcard(QualifiedTypeVariable<ProductSlot> subType,
                                                  QualifiedWildcardType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitWildcard_Array(QualifiedWildcardType<ProductSlot> subType,
                                           QualifiedArrayType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitWildcard_Declared(QualifiedWildcardType<ProductSlot> subType,
                                              QualifiedDeclaredType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitWildcard_Intersection(QualifiedWildcardType<ProductSlot> subType,
                                                  QualifiedIntersectionType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitWildcard_Primitive(QualifiedWildcardType<ProductSlot> subType,
                                               QualifiedPrimitiveType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitWildcard_TypeVariable(QualifiedWildcardType<ProductSlot> subType,
                                                  QualifiedTypeVariable<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        protected Void visitWildcard_Wildcard(QualifiedWildcardType<ProductSlot> subType,
                                              QualifiedWildcardType<ProductSlot> superType) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        // An intersection is a supertype if all of its bounds are a supertype of subtype.
        protected void visitType_Intersection(QualifiedType<ProductSlot> subType,
                                              QualifiedIntersectionType<ProductSlot> superType) {
            int prevConstraintCount = constraints.size();

            // TODO(generics): handle visited checks
            for (QualifiedType<ProductSlot> bound : superType.getBounds()) {
                if (TypesUtils.isErasedSubtype(subType.getJavaType(), bound.getJavaType(), types)) {
                    visit(subType, bound);
                }
            }

            assert constraints.size() > prevConstraintCount;
        }

        // An intersection is a subtype if one of its bounds is a subtype of {@code supertype}.
        // TODO: how to handle this correctly?
        protected void visitIntersection_Type(QualifiedIntersectionType<ProductSlot> subType,
                                              QualifiedType<ProductSlot> superType) {
            int prevConstraintCount = constraints.size();

            for (QualifiedType<ProductSlot> bound : subType.getBounds()) {
                if (TypesUtils.isErasedSubtype(bound.getJavaType(), superType.getJavaType(), types)) {
                    visit(bound, superType);
                }
            }

            assert constraints.size() > prevConstraintCount;
        }

        // A union type is a subtype if ALL of its alternatives are subtypes of supertype.
        protected void visitUnion_Type(QualifiedUnionType<ProductSlot> subType,
                                       QualifiedType<ProductSlot> superType) {
            int prevConstraintCount = constraints.size();

            for (QualifiedType<ProductSlot> alt : subType.getAlternatives()) {
                visit(alt, superType);
            }

            assert constraints.size() > prevConstraintCount;
        }

        protected void visitType_Union(QualifiedType<ProductSlot> subType,
                                       QualifiedUnionType<ProductSlot> superType) {
            int prevConstraintCount = constraints.size();

            for (QualifiedType<ProductSlot> alt : superType.getAlternatives()) {
                if (TypesUtils.isErasedSubtype(subType.getJavaType(), alt.getJavaType(), types)) {
                    // Alternatives in a union type should be disjoint, thus there should only be
                    // one alt that satisfies the check (unless subType is NullType).
                    visit(subType, alt);
                }
            }

            assert constraints.size() > prevConstraintCount;
        }
    }

    protected class EqualityVisitor extends DoubleQualifiedTypeScanner<ProductSlot, ProductSlot, Void> {

        protected SetMultimap<QualifierHierarchy, Constraint> constraints = LinkedHashMultimap.create();

        private void addConstraints(PrimaryQualifiedType<ProductSlot> type1, PrimaryQualifiedType<ProductSlot> type2) {
            SetMultimap<QualifierHierarchy, Constraint> equalityConstraints =
                    slotQualifierHierarchy.getEqualityConstraints(
                            type1.getQualifier(),
                            type2.getQualifier(),
                            qualifierHierarchies
                    );
            constraints.putAll(equalityConstraints);
        }

        @Override
        public Void visitArray(QualifiedArrayType<ProductSlot> type1, QualifiedArrayType<ProductSlot> type2) {
            addConstraints(type1, type2);
            return super.visitArray(type1, type2);
        }

        @Override
        public Void visitDeclared(QualifiedDeclaredType<ProductSlot> type1, QualifiedDeclaredType<ProductSlot> type2) {
            addConstraints(type1, type2);
            return super.visitDeclared(type1, type2);
        }

        @Override
        public Void visitNull(QualifiedNullType<ProductSlot> type1, QualifiedNullType<ProductSlot> type2) {
            addConstraints(type1, type2);
            return super.visitNull(type1, type2);
        }

        @Override
        public Void visitPrimitive(QualifiedPrimitiveType<ProductSlot> type1,
                                   QualifiedPrimitiveType<ProductSlot> type2) {
            addConstraints(type1, type2);
            return super.visitPrimitive(type1, type2);
        }

        @Override
        public Void visitTypeVariable(QualifiedTypeVariable<ProductSlot> type1,
                                      QualifiedTypeVariable<ProductSlot> type2) {
            addConstraints(type1, type2);
            return super.visitTypeVariable(type1, type2);
        }

        @Override
        public Void visitWildcard(QualifiedWildcardType<ProductSlot> type1, QualifiedWildcardType<ProductSlot> type2) {
            addConstraints(type1, type2);
            return super.visitWildcard(type1, type2);
        }
    }
}
