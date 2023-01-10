package org.cfginference.core.model.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IntersectionTypeTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.flow.FlowContext;
import org.cfginference.core.model.element.PrimaryQualifiedElement;
import org.cfginference.core.model.element.QualifiedElement;
import org.cfginference.core.model.element.QualifiedExecutableElement;
import org.cfginference.core.model.element.QualifiedRecordComponentElement;
import org.cfginference.core.model.element.QualifiedTypeElement;
import org.cfginference.core.model.element.QualifiedTypeParameterElement;
import org.cfginference.core.model.element.QualifiedVariableElement;
import org.cfginference.core.model.reporting.PluginError;
import org.cfginference.core.model.location.ASTLocation;
import org.cfginference.core.model.location.LocationManager;
import org.cfginference.core.model.location.QualifierLocation;
import org.cfginference.core.model.slot.ProductSlot;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.slot.SourceSlot;
import org.cfginference.core.model.slot.VariableSlot;
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
import org.cfginference.util.ASTPathUtils;
import org.cfginference.util.TreeHelpers;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scenelib.annotations.io.ASTPath;
import scenelib.annotations.io.ASTRecord;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

// TODO: handle artificial trees
public class SlotLocator {

    private static final Logger logger = LoggerFactory.getLogger(SlotLocator.class);

    private static final Set<Tree.Kind> supportedTreeKinds = Set.of(
            Tree.Kind.CLASS,
            Tree.Kind.ENUM,
            Tree.Kind.INTERFACE,
            Tree.Kind.ANNOTATION_TYPE,
            Tree.Kind.NEW_CLASS,
            Tree.Kind.METHOD,
            Tree.Kind.VARIABLE
    );

    private final Context context;

    private final ImplicitLocationLocator implicitLocationLocator;

    private final FlowContext flowContext;

    private final LocationManager locationManager;

    private final IdentityHashMap<Slot, QualifierLocation> locations;

    private final TypeSourceSlotLocator typeSourceSlotLocator;

    private final ElementSourceSlotLocator elementSourceSlotLocator;

    private final TypeSourceSlotLocationApplier typeSourceSlotLocationApplier;

    private final ElementSourceSlotLocationApplier elementSourceSlotLocationApplier;

    private SlotLocator(Context context) {
        this.context = context;
        this.flowContext = FlowContext.instance(context);
        this.locationManager = LocationManager.instance(context);
        this.locations = new IdentityHashMap<>();
        this.typeSourceSlotLocationApplier = new TypeSourceSlotLocationApplier();
        this.elementSourceSlotLocationApplier = new ElementSourceSlotLocationApplier();
        this.implicitLocationLocator = new ImplicitLocationLocator();
        this.typeSourceSlotLocator = new TypeSourceSlotLocator();
        this.elementSourceSlotLocator = new ElementSourceSlotLocator();

        context.put(SlotLocator.class, this);
    }

    public static SlotLocator instance(Context context) {
        SlotLocator locator = context.get(SlotLocator.class);
        if (locator == null) {
            locator = new SlotLocator(context);
        }
        return locator;
    }

    public @Nullable QualifierLocation getLocation(Slot slot) {
        return locations.get(slot);
    }

    public Map<Slot, QualifierLocation> getLocations() {
        return Collections.unmodifiableMap(locations);
    }

    public void addLocation(Slot slot, QualifierLocation location) {
        if (!(slot instanceof VariableSlot)) {
            // non-variable slots shouldn't have locations
            return;
        }

        QualifierLocation existingLocation = locations.get(slot);

        if (!location.equals(existingLocation)) {
            if (existingLocation == null) {
                locations.put(slot, location);
            } else {
                throw new IllegalStateException("""
                        Multiple locations detected for the same slot %s,
                        location 1: %s,
                        location 2: %s""".formatted(slot, existingLocation, location));
            }
        }
    }

    public void addLocation(ProductSlot productSlot, QualifierLocation location, boolean sourceSlotOnly) {
        for (Slot slot : productSlot.getSlots().values()) {
            if (!sourceSlotOnly || slot instanceof SourceSlot) {
                addLocation(slot, location);
            }
        }
    }

    private void addLocation(ProductSlot productSlot, Tree tree, boolean insertable, boolean sourceSlotOnly) {
        addLocation(productSlot, locationManager.getASTLocation(tree, insertable), sourceSlotOnly);
    }

    private void addLocation(ProductSlot productSlot, Tree tree, boolean sourceSlotOnly) {
        addLocation(productSlot, tree, true, sourceSlotOnly);
    }

    public void locateSourceSlots(QualifiedElement<ProductSlot> declType, Tree tree) {
        Preconditions.checkArgument(supportedTreeKinds.contains(tree.getKind()));

        if (flowContext.isArtificialTree(tree)) {
            TreePath artificialTreePath = flowContext.getPathToArtificialTreeParent(tree);
            QualifierLocation location = locationManager.getASTLocation(artificialTreePath.getLeaf(), false);
            elementSourceSlotLocationApplier.scan(declType, location);
        } else {
            elementSourceSlotLocator.visit(declType, tree);
        }
    }

    private class TypeSourceSlotLocationApplier
            extends PrimaryQualifiedTypeScanner<ProductSlot, Void, QualifierLocation> {

        @Override
        protected void visitPrimaryQualifiedType(PrimaryQualifiedType<ProductSlot> type,
                                                 QualifierLocation location) {
            addLocation(type.getQualifier(), location, true);
        }
    }

    private class ElementSourceSlotLocationApplier
            extends PrimaryQualifiedElementScanner<ProductSlot, Void, QualifierLocation> {

        @Override
        protected void visitPrimaryQualifiedElement(PrimaryQualifiedElement<ProductSlot> element,
                                                    QualifierLocation location) {
            addLocation(element.getQualifier(), location, true);
        }

        @Override
        public Void visitType(QualifiedTypeElement<ProductSlot> element,
                              QualifierLocation location) {
            typeSourceSlotLocationApplier.scan(element.getInterfaces(), location);
            typeSourceSlotLocationApplier.scan(element.getSuperClass(), location);
            return super.visitType(element, location);
        }

        @Override
        public Void visitTypeParameter(QualifiedTypeParameterElement<ProductSlot> element,
                                       QualifierLocation location) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        public Void visitExecutable(QualifiedExecutableElement<ProductSlot> element,
                                    QualifierLocation location) {
            typeSourceSlotLocationApplier.scan(element.getReceiverType(), location);
            typeSourceSlotLocationApplier.scan(element.getThrownTypes(), location);
            typeSourceSlotLocationApplier.scan(element.getReturnType(), location);
            return super.visitExecutable(element, location);
        }

        @Override
        public Void visitRecordComponent(QualifiedRecordComponentElement<ProductSlot> element,
                                         QualifierLocation location) {
            typeSourceSlotLocationApplier.scan(element.getType(), location);
            return super.visitRecordComponent(element, location);
        }

        @Override
        public Void visitVariable(QualifiedVariableElement<ProductSlot> element,
                                  QualifierLocation location) {
            typeSourceSlotLocationApplier.scan(element.getType(), location);
            return super.visitVariable(element, location);
        }
    }

    private class ElementSourceSlotLocator implements QualifiedElementVisitor<ProductSlot, Void, Tree> {

        public void visit(QualifiedElement<ProductSlot> element, Tree tree) {
            element.accept(this, tree);
        }

        public void visit(List<? extends QualifiedElement<ProductSlot>> elements, List<? extends Tree> trees) {
            Preconditions.checkArgument(elements.size() == trees.size());
            for (int i = 0; i < elements.size(); ++i) {
                visit(elements.get(i), trees.get(i));
            }
        }

        @Override
        public Void visitExecutable(QualifiedExecutableElement<ProductSlot> element, Tree tree) {
            Preconditions.checkArgument(tree.getKind() == Tree.Kind.METHOD);

            MethodTree methodTree = (MethodTree) tree;
            visit(element.getParameters(), methodTree.getParameters());
            visit(element.getTypeParameters(), methodTree.getTypeParameters());
            typeSourceSlotLocator.visit(element.getThrownTypes(), methodTree.getThrows());

            if (methodTree.getReturnType() == null) {
                implicitLocationLocator.implicitConstructorReturnType(element.getReturnType(), methodTree);
            } else {
                typeSourceSlotLocator.visit(element.getReturnType(), methodTree.getReturnType());
            }

            if (methodTree.getReceiverParameter() == null) {
                implicitLocationLocator.implicitReceiver(element.getReceiverType(), methodTree);
            } else {
                typeSourceSlotLocator.visit(element.getReceiverType(), ((MethodTree) tree).getReceiverParameter());
            }
            return null;
        }

        @Override
        public Void visitRecordComponent(QualifiedRecordComponentElement<ProductSlot> element, Tree tree) {
            // TODO(record): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        public Void visitType(QualifiedTypeElement<ProductSlot> element, Tree tree) {
            // TODO(record): implementation
            // The way to declare record components in the JCTree is different from the source code,
            // so we may need to modify ASTRecord
            Preconditions.checkArgument(tree.getKind() == Tree.Kind.CLASS
                    || tree.getKind() == Tree.Kind.NEW_CLASS
                    || tree.getKind() == Tree.Kind.INTERFACE
                    || tree.getKind() == Tree.Kind.ENUM
                    || tree.getKind() == Tree.Kind.ANNOTATED_TYPE);

            if (tree.getKind() == Tree.Kind.NEW_CLASS) {
                NewClassTree newClassTree = (NewClassTree) tree;
                Verify.verify(newClassTree.getClassBody() != null,
                        "Can only process anonymous class, found tree: %s",
                        tree);
                Verify.verify(element.getTypeParameters().isEmpty() && element.getRecordComponents().isEmpty(),
                        "Anonymous class shouldn't have type params or record components.");

                addLocation(element.getQualifier(), newClassTree.getIdentifier(), true);

                if (element.getInterfaces().isEmpty()) {
                    // extending a class
                    typeSourceSlotLocator.visit(element.getSuperClass(), newClassTree);
                } else {
                    // implementing an interface
                    Verify.verify(element.getInterfaces().size() == 1);
                    typeSourceSlotLocator.visit(element.getInterfaces().get(0), newClassTree);
                }
            } else {
                ClassTree classTree = (ClassTree) tree;
                TypeElement typeElement = element.getJavaElement();

                // TODO: does this work for local class?
                addLocation(
                        element.getQualifier(),
                        locationManager.getClassDeclLocation(typeElement),
                        true
                );

                if (classTree.getExtendsClause() != null) {
                    typeSourceSlotLocator.visit(element.getSuperClass(), classTree.getExtendsClause());
                } else {
                    implicitLocationLocator.implicitExtendsClause(element.getSuperClass(), classTree);
                }
                typeSourceSlotLocator.visit(element.getInterfaces(), classTree.getImplementsClause());
                visit(element.getTypeParameters(), classTree.getTypeParameters());
            }
            return null;
        }

        @Override
        public Void visitTypeParameter(QualifiedTypeParameterElement<ProductSlot> element, Tree tree) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        public Void visitVariable(QualifiedVariableElement<ProductSlot> element, Tree tree) {
            Preconditions.checkArgument(tree.getKind() == Tree.Kind.VARIABLE);

            typeSourceSlotLocator.visit(element.getType(), tree);
            return null;
        }
    }

    private class TypeSourceSlotLocator implements QualifiedTypeVisitor<ProductSlot, Void, Tree> {

        private Tree unwrapTree(Tree tree) {
            Tree unwrappedTree = tree;
            while (true) {
                if (unwrappedTree.getKind() == Tree.Kind.ANNOTATED_TYPE) {
                    unwrappedTree = ((AnnotatedTypeTree) unwrappedTree).getUnderlyingType();
                } else if (unwrappedTree.getKind() == Tree.Kind.VARIABLE) {
                    unwrappedTree = ((VariableTree) unwrappedTree).getType();
                } else if (unwrappedTree.getKind() == Tree.Kind.PARENTHESIZED) {
                    unwrappedTree = ((ParenthesizedTree) unwrappedTree).getExpression();
                } else {
                    break;
                }
            }
            return unwrappedTree;
        }

        private void visit(List<? extends QualifiedType<ProductSlot>> types, List<? extends Tree> trees) {
            Preconditions.checkArgument(types.size() == trees.size());
            for (int i = 0; i < types.size(); ++i) {
                visit(types.get(i), trees.get(i));
            }
        }

        public Void visit(QualifiedType<ProductSlot> type, Tree tree) {
            return type.accept(this, unwrapTree(tree));
        }

        @Override
        public Void visitArray(QualifiedArrayType<ProductSlot> type, Tree tree) {
            switch (tree.getKind()) {
                case ARRAY_TYPE -> {
                    ArrayTypeTree typeTree = (ArrayTypeTree) tree;
                    addLocation(type.getQualifier(), typeTree, true);
                    visit(type.getComponentType(), typeTree.getType());
                }
                case NEW_ARRAY -> {
                    // e.g., {1, 2}, new int[]{1,2}
                    NewArrayTree newArrayTree = (NewArrayTree) tree;
                    ASTRecord astRecord = ASTPathUtils.getASTRecord(flowContext.getRoot(), newArrayTree);
                    boolean isArrayLiteral = TreeHelpers.isArrayLiteral(newArrayTree);

                    int level = 0;
                    QualifiedType<ProductSlot> currentType = type;
                    while (currentType instanceof QualifiedArrayType<ProductSlot> currentArrayType) {
                        addLocation(
                                currentArrayType.getQualifier(),
                                locationManager.getASTLocation(astRecord.newArrayLevel(level), !isArrayLiteral),
                                true
                        );
                        currentType = currentArrayType.getComponentType();
                        ++level;
                    }

                    // the innermost component type has to be a non-nested, primarily qualified type
                    Verify.verify(currentType instanceof PrimaryQualifiedType);
                    addLocation(
                            ((PrimaryQualifiedType<ProductSlot>) currentType).getQualifier(),
                            locationManager.getASTLocation(astRecord.newArrayLevel(level), !isArrayLiteral),
                            true
                    );
                }
                default -> throw new PluginError("Unexpected tree %s for array type", tree);
            }
            return null;
        }

        @Override
        public Void visitDeclared(QualifiedDeclaredType<ProductSlot> type, Tree tree) {
            // Note: `type.getEnclosingType()` will return a NoType if it's an inner static class
            switch (tree.getKind()) {
                // TODO(generics): case PARAMETERIZED_TYPE
                case IDENTIFIER -> {
                    IdentifierTree identifierTree = (IdentifierTree) tree;
                    addLocation(type.getQualifier(), identifierTree, true);

                    if (type.getEnclosingType().getJavaType().getKind() != TypeKind.NONE) {
                        implicitLocationLocator.implicitEnclosingType(type, identifierTree);
                    }
                }
                case NEW_CLASS -> {
                    NewClassTree newClassTree = (NewClassTree) tree;
                    addLocation(type.getQualifier(), newClassTree.getIdentifier(), true);

                    QualifiedType<ProductSlot> enclosingType = type.getEnclosingType();
                    if (enclosingType.getJavaType().getKind() != TypeKind.NONE) {
                        ExpressionTree enclosingExpr = newClassTree.getEnclosingExpression();
                        if (enclosingExpr != null) {
                            enclosingExpr = TreeUtils.withoutParens(newClassTree.getEnclosingExpression());
                        }

                        if (enclosingExpr != null && enclosingExpr.getKind() == Tree.Kind.NEW_CLASS) {
                            // case 1: new Outer().new Inner()
                            visit(enclosingType, enclosingExpr);
                        } else {
                            // case 2: (explicit/implicit expr.)new Inner()
                            implicitLocationLocator.implicitEnclosingType(type, newClassTree);
                        }
                    }
                }
                case MEMBER_SELECT -> {
                    MemberSelectTree memberSelectTree = (MemberSelectTree) tree;
                    addLocation(type.getQualifier(), memberSelectTree, true);

                    QualifiedType<ProductSlot> enclosingType = type.getEnclosingType();
                    ExpressionTree expr = TreeUtils.withoutParens(memberSelectTree.getExpression());
                    if (enclosingType.getJavaType().getKind() != TypeKind.NONE) {
                        visit(enclosingType, expr);
                    }
                }
                default -> throw new PluginError("Unexpected tree %s for declared type", tree);
            }
            return null;
        }

        @Override
        public Void visitExecutable(QualifiedExecutableType<ProductSlot> type, Tree tree) {
            // Preconditions.checkArgument(tree.getKind() == Tree.Kind.METHOD_INVOCATION);
            // MethodInvocationTree methodInvocationTree = (MethodInvocationTree) tree;
            //
            // ASTLocation astLocation = locationManager.getASTLocation(methodInvocationTree, false);
            // typeSourceSlotLocationApplier.scan(type.getReceiverType(), astLocation);
            // typeSourceSlotLocationApplier.scan(type.getThrownTypes(), astLocation);
            // typeSourceSlotLocationApplier.scan(type.getReturnType(), astLocation);
            //
            // ExecutableElement methodElt = TreeUtils.elementFromUse(methodInvocationTree);
            // int nonVarargs = methodElt.getParameters().size();
            // if (methodElt.isVarArgs()) {
            //     --nonVarargs;
            // }
            //
            // // TODO: handle varargs
            // for (int i = 0; i < nonVarargs; ++i) {
            //     ExpressionTree argTree = methodInvocationTree.getArguments().get(i);
            //     astLocation = locationManager.getASTLocation(argTree, false);
            //     typeSourceSlotLocationApplier.scan(type.getParameterTypes().get(i), astLocation);
            // }

            return null;
        }

        @Override
        public Void visitIntersection(QualifiedIntersectionType<ProductSlot> type, Tree tree) {
            Preconditions.checkArgument(tree.getKind() == Tree.Kind.INTERSECTION_TYPE);
            IntersectionTypeTree intersectionTypeTree = (IntersectionTypeTree) tree;
            addLocation(type.getQualifier(), intersectionTypeTree, false);
            visit(type.getBounds(), intersectionTypeTree.getBounds());
            return null;
        }

        @Override
        public Void visitNo(QualifiedNoType<ProductSlot> type, Tree tree) {
            return null;
        }

        @Override
        public Void visitNull(QualifiedNullType<ProductSlot> type, Tree tree) {
            throw new PluginError("Source slot should never annotate null type");
        }

        @Override
        public Void visitPrimitive(QualifiedPrimitiveType<ProductSlot> type, Tree tree) {
            addLocation(type.getQualifier(), tree, true);
            return null;
        }

        @Override
        public Void visitTypeVariable(QualifiedTypeVariable<ProductSlot> type, Tree tree) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        public Void visitUnion(QualifiedUnionType<ProductSlot> type, Tree tree) {
            Preconditions.checkArgument(tree.getKind() == Tree.Kind.UNION_TYPE);
            UnionTypeTree unionTypeTree = (UnionTypeTree) tree;
            addLocation(type.getQualifier(), unionTypeTree, false);
            visit(type.getAlternatives(), unionTypeTree.getTypeAlternatives());
            return null;
        }

        @Override
        public Void visitWildcard(QualifiedWildcardType<ProductSlot> type, Tree tree) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }
    }

    // Resolves the location for a qualifier that cannot be applied without modifying the source code.
    // For example, all the annotatable locations in an implicit constructor.
    private class ImplicitLocationLocator {
        public void implicitExtendsClause(QualifiedType<ProductSlot> type, ClassTree parentClassTree) {
            if (type instanceof PrimaryQualifiedType<ProductSlot> objectType) {
                // Implicit superclass can only be java.lang.Object or NoType.
                // NoType is not a PrimaryQualifiedType, so we only need to consider the first case.
                Verify.verify(TypesUtils.isObject(objectType.getJavaType()));
                ASTRecord astRecord = ASTPathUtils.getASTRecord(flowContext.getRoot(), parentClassTree)
                        .extend(Tree.Kind.CLASS, ASTPath.BOUND, -1);
                addLocation(objectType.getQualifier(),
                        locationManager.getASTLocation(astRecord, true),
                        true);
            }
        }

        public void implicitReceiver(QualifiedType<ProductSlot> type, MethodTree parentMethodTree) {
            if (type.getJavaType().getKind() == TypeKind.NONE) {
                return;
            }

            ExecutableElement methodElement = TreeUtils.elementFromDeclaration(parentMethodTree);
            // Currently inner class constructors throw an exception in the AFU
            boolean insertable = methodElement.getKind() != ElementKind.CONSTRUCTOR;

            ASTRecord astRecord =
                    Objects.requireNonNull(ASTPathUtils.getASTRecord(flowContext.getRoot(), parentMethodTree));
            ASTRecord toReceiverType = astRecord.extend(Tree.Kind.METHOD, ASTPath.PARAMETER, -1)
                    .extend(Tree.Kind.VARIABLE, ASTPath.TYPE);
            IdentityHashMap<PrimaryQualifiedType<ProductSlot>, ASTRecord> typesToRecords =
                    ASTPathUtils.getImpliedRecordForUse(toReceiverType, type);

            for (Map.Entry<PrimaryQualifiedType<ProductSlot>, ASTRecord> entry : typesToRecords.entrySet()) {
                ASTLocation location = locationManager.getASTLocation(entry.getValue(), insertable);
                addLocation(entry.getKey().getQualifier(), location, true);
            }
        }

        public void implicitConstructorReturnType(QualifiedType<ProductSlot> type, MethodTree parentMethodTree) {
            if (type instanceof QualifiedDeclaredType<ProductSlot> declaredType) {
                QualifierLocation insertableLocation = locationManager.getASTLocation(parentMethodTree, true);
                QualifierLocation nonInsertableLocation = locationManager.getASTLocation(parentMethodTree, false);
                addLocation(declaredType.getQualifier(), insertableLocation, true);
                typeSourceSlotLocationApplier.scan(declaredType.getEnclosingType(), nonInsertableLocation);
                typeSourceSlotLocationApplier.scan(declaredType.getTypeArguments(), nonInsertableLocation);
            }
        }

        public void implicitEnclosingType(QualifiedDeclaredType<ProductSlot> declaredType,
                                          Tree parentTree) {
            ASTRecord toEnclosingExpr = ASTPathUtils.getASTRecord(flowContext.getRoot(), parentTree)
                    .extend(Tree.Kind.MEMBER_SELECT, ASTPath.EXPRESSION);
            IdentityHashMap<PrimaryQualifiedType<ProductSlot>, ASTRecord> typesToRecords =
                    ASTPathUtils.getImpliedRecordForUse(toEnclosingExpr, declaredType);

            for (Map.Entry<PrimaryQualifiedType<ProductSlot>, ASTRecord> entry : typesToRecords.entrySet()) {
                ASTLocation location = locationManager.getASTLocation(entry.getValue(), false);
                addLocation(entry.getKey().getQualifier(), location, true);
            }
        }

        // TODO(generics): implementation
        // public void implicitTypeArguments() {}
    }
}
