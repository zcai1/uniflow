package org.cfginference.core.model.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IntersectionTypeTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.event.Event;
import org.cfginference.core.event.EventListener;
import org.cfginference.core.event.EventManager;
import org.cfginference.core.model.element.QualifiedElement;
import org.cfginference.core.model.element.QualifiedExecutableElement;
import org.cfginference.core.model.element.QualifiedRecordComponentElement;
import org.cfginference.core.model.element.QualifiedTypeElement;
import org.cfginference.core.model.element.QualifiedTypeParameterElement;
import org.cfginference.core.model.element.QualifiedVariableElement;
import org.cfginference.core.model.error.PluginError;
import org.cfginference.core.model.location.ASTLocation;
import org.cfginference.core.model.location.ClassDeclLocation;
import org.cfginference.core.model.location.QualifierLocation;
import org.cfginference.core.model.slot.Slot;
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
import org.cfginference.util.TreeHelpers;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;
import scenelib.annotations.io.ASTIndex;
import scenelib.annotations.io.ASTPath;
import scenelib.annotations.io.ASTRecord;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: handle artificial trees
public class SlotLocator implements EventListener {

    private final ImplicitLocationLocator implicitLocationLocator;

    private final Map<Integer, QualifierLocation> locations = new HashMap<>();

    private CompilationUnitTree currentRoot = null;

    public SlotLocator(Context context) {
        implicitLocationLocator = new ImplicitLocationLocator();
        // TODO: this cannot receive events fired before initialization
        EventManager.instance(context).register(this);
    }

    @Override
    public void started(Event e) {
        if (e instanceof Event.NewAnalysisTask newAnalysisTask) {
            currentRoot = newAnalysisTask.root();
        }
    }

    public void addLocation(Slot slot, QualifierLocation location) {
        Integer id = slot.getId();
        QualifierLocation existingLocation = locations.get(id);

        if (!location.equals(existingLocation)) {
            if (existingLocation == null) {
                locations.put(id, location);
            } else {
                throw new IllegalStateException("""
                        Multiple locations detected for the same slot %s,
                        location 1: %s,
                        location 2: %s""".formatted(id, existingLocation, location));
            }
        }
    }

    private void addLocation(Slot slot, Tree tree) {
        addLocation(slot, tree, true);
    }

    private void addLocation(Slot slot, Tree tree, boolean insertable) {
        addLocation(slot, new ASTLocation(ASTIndex.getASTPath(currentRoot, tree), insertable));
    }

    private class SimpleLocationApplier<S extends Slot> extends QualifiedTypeScanner<S, Void, QualifierLocation> {
        @Override
        public Void visitArray(QualifiedArrayType<S> type, QualifierLocation qualifierLocation) {
            addLocation(type.getQualifier(), qualifierLocation);
            return super.visitArray(type, qualifierLocation);
        }

        @Override
        public Void visitDeclared(QualifiedDeclaredType<S> type, QualifierLocation qualifierLocation) {
            addLocation(type.getQualifier(), qualifierLocation);
            return super.visitDeclared(type, qualifierLocation);
        }

        @Override
        public Void visitIntersection(QualifiedIntersectionType<S> type, QualifierLocation qualifierLocation) {
            addLocation(type.getQualifier(), qualifierLocation);
            return super.visitIntersection(type, qualifierLocation);
        }

        @Override
        public Void visitNull(QualifiedNullType<S> type, QualifierLocation qualifierLocation) {
            addLocation(type.getQualifier(), qualifierLocation);
            return super.visitNull(type, qualifierLocation);
        }

        @Override
        public Void visitPrimitive(QualifiedPrimitiveType<S> type, QualifierLocation qualifierLocation) {
            addLocation(type.getQualifier(), qualifierLocation);
            return super.visitPrimitive(type, qualifierLocation);
        }

        @Override
        public Void visitTypeVariable(QualifiedTypeVariable<S> type, QualifierLocation qualifierLocation) {
            addLocation(type.getQualifier(), qualifierLocation);
            return super.visitTypeVariable(type, qualifierLocation);
        }

        @Override
        public Void visitUnion(QualifiedUnionType<S> type, QualifierLocation qualifierLocation) {
            addLocation(type.getQualifier(), qualifierLocation);
            return super.visitUnion(type, qualifierLocation);
        }

        @Override
        public Void visitWildcard(QualifiedWildcardType<S> type, QualifierLocation qualifierLocation) {
            addLocation(type.getQualifier(), qualifierLocation);
            return super.visitWildcard(type, qualifierLocation);
        }
    }

    private class ElementSlotLocator<S extends Slot> implements QualifiedElementVisitor<S, Void, Tree> {

        private final TypeSlotLocator<S> typeScanner;

        public ElementSlotLocator(TypeSlotLocator<S> typeScanner) {
            this.typeScanner = typeScanner;
        }

        public void visit(QualifiedElement<S> element, Tree tree) {
            element.accept(this, tree);
        }

        public void visit(List<? extends QualifiedElement<S>> elements, List<? extends Tree> trees) {
            Preconditions.checkArgument(elements.size() == trees.size());
            for (int i = 0; i < elements.size(); ++i) {
                visit(elements.get(i), trees.get(i));
            }
        }

        @Override
        public Void visitExecutable(QualifiedExecutableElement<S> element, Tree tree) {
            Preconditions.checkArgument(tree.getKind() == Tree.Kind.METHOD);

            MethodTree methodTree = (MethodTree) tree;
            visit(element.getParameters(), methodTree.getParameters());
            visit(element.getTypeParameters(), methodTree.getTypeParameters());
            typeScanner.visit(element.getThrownTypes(), methodTree.getThrows());

            if (methodTree.getReturnType() == null) {
                implicitLocationLocator.implicitConstructorReturnType(element.getReturnType(), methodTree);
            } else {
                typeScanner.visit(element.getReturnType(), methodTree.getReturnType());
            }

            if (methodTree.getReceiverParameter() == null) {
                implicitLocationLocator.implicitReceiver(element.getReceiverType(), methodTree);
            } else {
                typeScanner.visit(element.getReceiverType(), ((MethodTree) tree).getReceiverParameter());
            }

            return null;
        }

        @Override
        public Void visitRecordComponent(QualifiedRecordComponentElement<S> element, Tree tree) {
            typeScanner.visit(element.getType(), tree);
            return null;
        }

        @Override
        public Void visitType(QualifiedTypeElement<S> element, Tree tree) {
            Preconditions.checkArgument(tree.getKind() == Tree.Kind.CLASS || tree.getKind() == Tree.Kind.NEW_CLASS);

            if (tree.getKind() == Tree.Kind.CLASS) {
                ClassTree classTree = (ClassTree) tree;
                TypeElement typeElement = element.getJavaElement();

                // TODO(record): implementation
                // The way to declare record components in the JCTree is different from the source code,
                // so we may need to modify ASTRecord
                Verify.verify(element.getRecordComponents().isEmpty(),
                        "Record class is not supported");

                // TODO: does this work for local class?
                addLocation(
                        element.getQualifier(),
                        new ClassDeclLocation(((Symbol.ClassSymbol)typeElement).flatName().toString())
                );

                if (classTree.getExtendsClause() != null) {
                    typeScanner.visit(element.getSuperClass(), classTree.getExtendsClause());
                } else {
                    implicitLocationLocator.implicitExtendsClause(element.getSuperClass(), classTree);
                }
                typeScanner.visit(element.getInterfaces(), classTree.getImplementsClause());
                visit(element.getTypeParameters(), classTree.getTypeParameters());
            } else if (tree.getKind() == Tree.Kind.NEW_CLASS) {
                NewClassTree newClassTree = (NewClassTree) tree;
                Verify.verify(newClassTree.getClassBody() != null,
                        "Can only process anonymous class, found tree: %s",
                        tree);
                Verify.verify(element.getTypeParameters().isEmpty() && element.getRecordComponents().isEmpty(),
                        "Anonymous class shouldn't have type params or record components.");

                addLocation(element.getQualifier(), newClassTree.getIdentifier());

                if (element.getInterfaces().isEmpty()) {
                    // extending a class
                    typeScanner.visit(element.getSuperClass(), newClassTree);
                } else {
                    // implementing an interface
                    Verify.verify(element.getInterfaces().size() == 1);
                    typeScanner.visit(element.getInterfaces().get(0), newClassTree);
                }
            } else {
                throw new PluginError("Cannot process type element for tree: %s", tree);
            }
            return null;
        }

        @Override
        public Void visitTypeParameter(QualifiedTypeParameterElement<S> element, Tree tree) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        public Void visitVariable(QualifiedVariableElement<S> element, Tree tree) {
            typeScanner.visit(element.getType(), tree);
            return null;
        }
    }

    private class TypeSlotLocator<S extends Slot> implements QualifiedTypeVisitor<S, Void, Tree> {

        private SimpleLocationApplier<S> locationApplier;

        public TypeSlotLocator(SimpleLocationApplier<S> locationApplier) {
            this.locationApplier = locationApplier;
        }

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

        private void visit(List<? extends QualifiedType<S>> types, List<? extends Tree> trees) {
            Preconditions.checkArgument(types.size() == trees.size());
            for (int i = 0; i < types.size(); ++i) {
                visit(types.get(i), trees.get(i));
            }
        }

        public Void visit(QualifiedType<S> type, Tree tree) {
            return type.accept(this, unwrapTree(tree));
        }

        @Override
        public Void visitArray(QualifiedArrayType<S> type, Tree tree) {
            switch (tree.getKind()) {
                case ARRAY_TYPE -> {
                    ArrayTypeTree typeTree = (ArrayTypeTree) tree;
                    addLocation(type.getQualifier(), typeTree);
                    visit(type.getComponentType(), typeTree.getType());
                }
                case NEW_ARRAY -> {
                    // e.g., {1, 2}, new int[]{1,2}
                    NewArrayTree newArrayTree = (NewArrayTree) tree;
                    ASTRecord astRecord = ASTIndex.getASTPath(currentRoot, newArrayTree);
                    boolean isArrayLiteral = TreeHelpers.isArrayLiteral(newArrayTree);

                    int level = 0;
                    QualifiedType<S> currentType = type;
                    while (currentType instanceof QualifiedArrayType<S> currentArrayType) {
                        addLocation(
                                currentArrayType.getQualifier(),
                                new ASTLocation(astRecord.newArrayLevel(level), !isArrayLiteral)
                        );
                        currentType = currentArrayType.getComponentType();
                        ++level;
                    }

                    // the innermost component type has to be a non-nested, primarily qualified type
                    Verify.verify(currentType instanceof PrimaryQualifiedType);
                    addLocation(
                            ((PrimaryQualifiedType<S>) currentType).getQualifier(),
                            new ASTLocation(astRecord.newArrayLevel(level), !isArrayLiteral)
                    );
                }
                default -> throw new PluginError("Unexpected tree %s for array type", tree);
            }
            return null;
        }

        @Override
        public Void visitDeclared(QualifiedDeclaredType<S> type, Tree tree) {
            // Note: `type.getEnclosingType()` will return a NoType if it's an inner static class
            switch (tree.getKind()) {
                // TODO(generics): case PARAMETERIZED_TYPE
                case IDENTIFIER -> {
                    IdentifierTree identifierTree = (IdentifierTree) tree;
                    addLocation(type.getQualifier(), identifierTree);

                    if (type.getEnclosingType() != null) {
                        implicitLocationLocator.implicitEnclosingType(type, identifierTree);
                    }
                }
                case NEW_CLASS -> {
                    NewClassTree newClassTree = (NewClassTree) tree;
                    addLocation(type.getQualifier(), newClassTree.getIdentifier());

                    QualifiedType<S> enclosingType = type.getEnclosingType();
                    if (enclosingType != null) {
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
                    addLocation(type.getQualifier(), memberSelectTree);

                    QualifiedType<S> enclosingType = type.getEnclosingType();
                    ExpressionTree expr = TreeUtils.withoutParens(memberSelectTree.getExpression());
                    if (enclosingType != null) {
                        visit(enclosingType, expr);
                    }
                }
                default -> throw new PluginError("Unexpected tree %s for declared type", tree);
            }
            return null;
        }

        @Override
        public Void visitExecutable(QualifiedExecutableType<S> type, Tree tree) {
            Preconditions.checkArgument(tree.getKind() == Tree.Kind.METHOD_INVOCATION);
            MethodInvocationTree methodInvocationTree = (MethodInvocationTree) tree;

            ASTLocation astLocation = new ASTLocation(ASTIndex.getASTPath(currentRoot, methodInvocationTree), false);
            locationApplier.scan(type.getReceiverType(), astLocation);
            locationApplier.scan(type.getThrownTypes(), astLocation);
            locationApplier.scan(type.getReturnType(), astLocation);

            ExecutableElement methodElt = TreeUtils.elementFromUse(methodInvocationTree);
            int nonVarargs = methodElt.getParameters().size();
            if (methodElt.isVarArgs()) {
                --nonVarargs;
            }

            for (int i = 0; i < nonVarargs; ++i) {
                ExpressionTree argTree = methodInvocationTree.getArguments().get(i);
                astLocation = new ASTLocation(ASTIndex.getASTPath(currentRoot, argTree), false);
                locationApplier.scan(type.getParameterTypes().get(i), astLocation);
            }
            // TODO: handle varargs
            return null;
        }

        @Override
        public Void visitIntersection(QualifiedIntersectionType<S> type, Tree tree) {
            Preconditions.checkArgument(tree.getKind() == Tree.Kind.INTERSECTION_TYPE);
            IntersectionTypeTree intersectionTypeTree = (IntersectionTypeTree) tree;
            addLocation(type.getQualifier(), intersectionTypeTree, false);
            visit(type.getBounds(), intersectionTypeTree.getBounds());
            return null;
        }

        @Override
        public Void visitNo(QualifiedNoType<S> type, Tree tree) {
            return null;
        }

        @Override
        public Void visitNull(QualifiedNullType<S> type, Tree tree) {
            addLocation(type.getQualifier(), tree, false);
            return null;
        }

        @Override
        public Void visitPrimitive(QualifiedPrimitiveType<S> type, Tree tree) {
            addLocation(type.getQualifier(), tree);
            return null;
        }

        @Override
        public Void visitTypeVariable(QualifiedTypeVariable<S> type, Tree tree) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        public Void visitUnion(QualifiedUnionType<S> type, Tree tree) {
            Preconditions.checkArgument(tree.getKind() == Tree.Kind.UNION_TYPE);
            UnionTypeTree unionTypeTree = (UnionTypeTree) tree;
            addLocation(type.getQualifier(), unionTypeTree, false);
            visit(type.getAlternatives(), unionTypeTree.getTypeAlternatives());
            return null;
        }

        @Override
        public Void visitWildcard(QualifiedWildcardType<S> type, Tree tree) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }
    }

    // Resolves the location for a qualifier that cannot be applied without modifying the source code.
    // For example, all the annotatable locations in an implicit constructor.
    private class ImplicitLocationLocator {
        public <S extends Slot> void implicitExtendsClause(QualifiedType<S> type, ClassTree parentClassTree) {
            if (type instanceof PrimaryQualifiedType<S> objectType) {
                // Implicit superclass can only be java.lang.Object or NoType.
                // NoType is not a PrimaryQualifiedType, so we only need to consider the first case.
                Verify.verify(TypesUtils.isObject(objectType.getJavaType()));
                ASTRecord astRecord = ASTIndex.getASTPath(currentRoot, parentClassTree)
                        .extend(Tree.Kind.CLASS, ASTPath.BOUND, -1);
                addLocation(objectType.getQualifier(), new ASTLocation(astRecord, true));
            }
        }

        public void implicitReceiver(QualifiedType<? extends Slot> type, MethodTree parentMethodTree) {
            // TODO: implementation
        }

        public void implicitDefaultConstructor(QualifiedType<? extends Slot> type, ClassTree parentClassTree) {
            // TODO: implementation
        }

        public <S extends Slot> void implicitConstructorReturnType(QualifiedType<S> type, MethodTree parentMethodTree) {
            if (type instanceof PrimaryQualifiedType<S> primaryQualifiedType) {
                addLocation(primaryQualifiedType.getQualifier(), parentMethodTree);
            }
            // TODO(generics): implementation
        }

        public void implicitEnclosingType(QualifiedDeclaredType<? extends Slot> declaredType, NewClassTree parentNewClassTree) {
            // TODO: implementation
        }

        public void implicitEnclosingType(QualifiedDeclaredType<? extends Slot> declaredType, IdentifierTree parentIdentifierTree) {
            // TODO: implementation
        }

        // TODO(generics): implementation
        // public void implicitTypeArguments() {}
    }
}
