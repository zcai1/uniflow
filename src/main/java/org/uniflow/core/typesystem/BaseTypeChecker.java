package org.uniflow.core.typesystem;

import com.google.common.collect.SetMultimap;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import org.uniflow.core.flow.FlowContext;
import org.uniflow.core.flow.FlowStore;
import org.uniflow.core.flow.FlowValue;
import org.uniflow.core.flow.GeneralContext;
import org.uniflow.core.flow.SlotQualifierHierarchy;
import org.uniflow.core.model.constraint.Constraint;
import org.uniflow.core.model.constraint.ConstraintManager;
import org.uniflow.core.model.element.QualifiedElement;
import org.uniflow.core.model.element.QualifiedExecutableElement;
import org.uniflow.core.model.element.QualifiedTypeElement;
import org.uniflow.core.model.element.QualifiedVariableElement;
import org.uniflow.core.model.reporting.AnalysisMessage;
import org.uniflow.core.model.reporting.PluginError;
import org.uniflow.core.model.slot.ProductSlot;
import org.uniflow.core.model.type.PrimaryQualifiedType;
import org.uniflow.core.model.type.QualifiedDeclaredType;
import org.uniflow.core.model.type.QualifiedExecutableType;
import org.uniflow.core.model.type.QualifiedType;
import org.uniflow.core.model.util.QualifiedTypeScanner;
import org.uniflow.core.model.util.SimpleQualifiedElementVisitor;
import org.uniflow.util.NodeUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.cfg.ControlFlowGraph;
import org.checkerframework.dataflow.cfg.UnderlyingAST;
import org.checkerframework.dataflow.cfg.block.SpecialBlock;
import org.checkerframework.dataflow.cfg.node.AbstractNodeVisitor;
import org.checkerframework.dataflow.cfg.node.AssignmentNode;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.ReturnNode;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BaseTypeChecker implements TypeChecker {

    private static final Logger logger = LoggerFactory.getLogger(BaseTypeChecker.class);

    protected final Elements elements;

    protected final Types types;

    protected final GeneralContext generalContext;

    protected final FlowContext flowContext;

    protected final Set<QualifierHierarchy> qualifierHierarchies;

    protected final SlotQualifierHierarchy slotQualifierHierarchy;

    protected final ConstraintManager constraintManager;

    protected final DeclarationTypeResolver declarationTypeResolver;

    protected final NodeTypeResolver nodeTypeResolver;

    protected final BaseDeclarationTypeChecker declarationTypeChecker;

    protected final BaseNodeTypeChecker nodeTypeChecker;

    protected final TypeHierarchy typeHierarchy;

    public BaseTypeChecker(Context context,
                           TypeSystem typeSystem) {
        this.types = JavacTypes.instance(context);
        this.elements = JavacElements.instance(context);
        this.generalContext = GeneralContext.instance(context);
        this.flowContext = FlowContext.instance(context);
        this.qualifierHierarchies = typeSystem.getQualifierHierarchies();
        this.slotQualifierHierarchy = SlotQualifierHierarchy.instance(context);
        this.constraintManager = ConstraintManager.instance(context);
        this.declarationTypeResolver = typeSystem.getDeclarationTypeResolver();
        this.nodeTypeResolver = typeSystem.getNodeTypeResolver();
        this.typeHierarchy = typeSystem.getTypeHierarchy();

        this.declarationTypeChecker = createDeclarationTypeChecker();
        this.nodeTypeChecker = createNodeTypeChecker();
    }

    @Override
    public void checkDeclaration(Element element, @Nullable Tree tree) {
        QualifiedElement<ProductSlot> qualifiedElement = declarationTypeResolver.getType(element);
        if (qualifiedElement instanceof QualifiedTypeElement<ProductSlot>
                || qualifiedElement instanceof QualifiedExecutableElement<ProductSlot>
                || qualifiedElement instanceof QualifiedVariableElement<ProductSlot>) {
            declarationTypeChecker.visit(qualifiedElement, tree);
        } else {
            logger.warn("Skipping declaration check for element: " + qualifiedElement);
        }
    }

    @Override
    public void checkNode(Node node, Map<Node, FlowValue> nodeValues) {
        node.accept(nodeTypeChecker, nodeValues);
    }

    @Override
    public void checkSpecialBlock(SpecialBlock specialBlock, FlowStore store) {
        // TODO: pre/post condition checks
    }

    protected BaseDeclarationTypeChecker createDeclarationTypeChecker() {
        return new BaseDeclarationTypeChecker();
    }

    protected BaseNodeTypeChecker createNodeTypeChecker() {
        return new BaseNodeTypeChecker();
    }

    protected class BaseDeclarationTypeChecker
            extends SimpleQualifiedElementVisitor<ProductSlot, Void, @Nullable Tree> {

        @Override
        public Void visitExecutable(QualifiedExecutableElement<ProductSlot> element, @Nullable Tree tree) {
            // TODO(generics): handle generics
            ExecutableElement javaElement = element.getJavaElement();
            TypeElement enclosingType = (TypeElement) javaElement.getEnclosingElement();
            MethodTree methodTree = null;
            Tree receiverParam = null;
            Tree returnTypeTree = null;
            List<? extends Tree> thrownTypeTrees = null;

            if (tree != null) {
                methodTree = (MethodTree) tree;
                receiverParam = methodTree.getReceiverParameter();
                returnTypeTree = methodTree.getReturnType();
                thrownTypeTrees = methodTree.getThrows();

                assert thrownTypeTrees.size() == element.getThrownTypes().size();
            }

            // params are handled by visitType
            checkTypeUseRecursively(element.getReturnType(), returnTypeTree != null ? returnTypeTree : javaElement);

            // exit early for anonymous constructor
            if (javaElement.getKind() == ElementKind.CONSTRUCTOR
                    && enclosingType.getNestingKind() == NestingKind.ANONYMOUS) {
                return null;
            }

            checkTypeUseRecursively(element.getReceiverType(), receiverParam != null ? receiverParam : javaElement);
            for (int i = 0; i < element.getThrownTypes().size(); ++i) {
                QualifiedType<ProductSlot> thrownType = element.getThrownTypes().get(i);
                Object thrownTypeSource = thrownTypeTrees != null ? thrownTypeTrees.get(i) : javaElement;
                checkTypeUseRecursively(thrownType, thrownTypeSource);
            }

            Set<? extends ExecutableElement> overriddenMethods =
                    ElementUtils.getOverriddenMethods(element.getJavaElement(), types);

            for (ExecutableElement overriddenMethod : overriddenMethods) {
                QualifiedExecutableElement<ProductSlot> overriddenMethodType =
                        declarationTypeResolver.getType(overriddenMethod);
                checkOverride(methodTree, element, overriddenMethodType);
            }
            return null;
        }

        @Override
        public Void visitType(QualifiedTypeElement<ProductSlot> element, @Nullable Tree tree) {
            // TODO(generics): handle generics
            TypeElement javaElement = element.getJavaElement();
            Tree extendsClause = null;
            List<? extends Tree> implClauses = null;

            if (tree != null) {
                ClassTree classTree = (ClassTree) tree;
                if (classTree.getExtendsClause() != null) {
                    extendsClause = classTree.getExtendsClause();
                }

                implClauses = classTree.getImplementsClause();
                assert implClauses.size() == element.getInterfaces().size();
            }

            Object extendsSource = extendsClause != null ? extendsClause : javaElement;
            checkTypeUseRecursively(element.getSuperClass(), extendsSource);

            for (int i = 0; i < element.getInterfaces().size(); ++i) {
                QualifiedType<ProductSlot> interfaceType = element.getInterfaces().get(i);
                Object interfaceSource = implClauses != null ? implClauses.get(i) : javaElement;
                checkTypeUseRecursively(interfaceType, interfaceSource);
            }

            ProductSlot declQualifier = element.getQualifier();
            ProductSlot superClauseQualifier = ((PrimaryQualifiedType<ProductSlot>) element.getSuperClass()).getQualifier();

            // @A class X extends @B Y ==> @A <: @B
            SetMultimap<QualifierHierarchy, Constraint> subtypeForExtends = slotQualifierHierarchy.getSubtypeConstraints(
                    declQualifier,
                    superClauseQualifier,
                    qualifierHierarchies
            );
            AnalysisMessage unsatMessage = AnalysisMessage.createError(
                    generalContext.getRoot(),
                    extendsClause != null ? extendsClause : javaElement,
                    "invalid.declaration.extends",
                    declQualifier,
                    superClauseQualifier
            );
            constraintManager.addExplainedConstraints(subtypeForExtends, unsatMessage);

            // @A class X implements @B Y ==> @A <: @B
            for (int i = 0; i < element.getInterfaces().size(); ++i) {
                QualifiedType<ProductSlot> interfaceType = element.getInterfaces().get(i);
                ProductSlot interfaceQualifier = ((PrimaryQualifiedType<ProductSlot>) interfaceType).getQualifier();
                Object interfaceSource = implClauses != null ? implClauses.get(i) : javaElement;
                SetMultimap<QualifierHierarchy, Constraint> subtypeForImpls = slotQualifierHierarchy.getSubtypeConstraints(
                        declQualifier,
                        interfaceQualifier,
                        qualifierHierarchies
                );
                unsatMessage = AnalysisMessage.createError(
                        generalContext.getRoot(),
                        interfaceSource,
                        "invalid.declaration.impl",
                        declQualifier,
                        interfaceQualifier
                );
                constraintManager.addExplainedConstraints(subtypeForImpls, unsatMessage);
            }

            return null;
        }

        @Override
        public Void visitVariable(QualifiedVariableElement<ProductSlot> element, @Nullable Tree tree) {
            if (ElementUtils.isLocalVariable(element.getJavaElement())
                    || ElementUtils.isBindingVariable(element.getJavaElement())) {
                // local variables are allowed to have @Top by default
                return null;
            }

            Object source = (tree != null) ? tree : element.getJavaElement();
            checkTypeUseRecursively(element.getType(), source);
            return null;
        }

        protected void checkTypeUseRecursively(QualifiedType<ProductSlot> type, Object source) {
            QualifiedTypeScanner<ProductSlot, Void, Void> scanner = new QualifiedTypeScanner<>() {
                @Override
                public Void visitDeclared(QualifiedDeclaredType<ProductSlot> type, Void unused) {
                    TypeElement typeElement = (TypeElement) type.getJavaType().asElement();
                    QualifiedTypeElement<ProductSlot> qualifiedTypeElement = declarationTypeResolver.getType(typeElement);
                    SetMultimap<QualifierHierarchy, Constraint> constraints = slotQualifierHierarchy.getSubtypeConstraints(
                            type.getQualifier(),
                            qualifiedTypeElement.getQualifier(),
                            qualifierHierarchies
                    );
                    AnalysisMessage unsatMessage = AnalysisMessage.createError(
                            generalContext.getRoot(),
                            source,
                            "incompatible.type.use",
                            type
                    );
                    constraintManager.addExplainedConstraints(constraints, unsatMessage);
                    return super.visitDeclared(type, unused);
                }
            };
            scanner.scan(type, null);
        }

        protected void checkOverride(@Nullable MethodTree thisMethodTree,
                                     QualifiedExecutableElement<ProductSlot> thisMethodType,
                                     QualifiedExecutableElement<ProductSlot> overriddenMethodType) {
            Element thisMethodJavaElement = thisMethodType.getJavaElement();
            Tree returnTypeTree = null;
            Tree receiverTypeTree = null;
            List<? extends Tree> paramTrees = null;

            if (thisMethodTree != null) {
                returnTypeTree = thisMethodTree.getReturnType();
                receiverTypeTree = thisMethodTree.getReceiverParameter();
                paramTrees = thisMethodTree.getParameters();
            }

            SetMultimap<QualifierHierarchy, Constraint> returnTypeConstraints = typeHierarchy.getSubtypeConstraints(
                    thisMethodType.getReturnType(),
                    overriddenMethodType.getReturnType());
            AnalysisMessage unsatMessage = AnalysisMessage.createError(
                    generalContext.getRoot(),
                    returnTypeTree != null ? returnTypeTree : thisMethodJavaElement,
                    "invalid.override.return.type",
                    thisMethodType.getReturnType(),
                    overriddenMethodType.getReturnType());
            constraintManager.addExplainedConstraints(returnTypeConstraints, unsatMessage);

            SetMultimap<QualifierHierarchy, Constraint> receiverTypeConstraints = typeHierarchy.getSubtypeConstraints(
                    overriddenMethodType.getReceiverType(),
                    thisMethodType.getReceiverType());
            unsatMessage = AnalysisMessage.createError(
                    generalContext.getRoot(),
                    receiverTypeTree != null ? receiverTypeTree : thisMethodJavaElement,
                    "invalid.override.receiver.type",
                    thisMethodType.getReceiverType(),
                    overriddenMethodType.getReceiverType());
            constraintManager.addExplainedConstraints(receiverTypeConstraints, unsatMessage);

            for (int i = 0; i < thisMethodType.getParameters().size(); ++i) {
                QualifiedVariableElement<ProductSlot> thisParamType = thisMethodType.getParameters().get(i);
                QualifiedVariableElement<ProductSlot> overriddenParamType = overriddenMethodType.getParameters().get(i);
                SetMultimap<QualifierHierarchy, Constraint> paramTypeConstraints = typeHierarchy.getSubtypeConstraints(
                        overriddenParamType.getType(),
                        thisParamType.getType());
                unsatMessage = AnalysisMessage.createError(
                        generalContext.getRoot(),
                        paramTrees != null ? paramTrees.get(i) : thisMethodJavaElement,
                        "invalid.override.param.type",
                        thisParamType.getType(),
                        overriddenParamType.getType());
                constraintManager.addExplainedConstraints(paramTypeConstraints, unsatMessage);
            }
        }
    }

    protected class BaseNodeTypeChecker extends AbstractNodeVisitor<Void, Map<Node, FlowValue>> {

        @Override
        public Void visitNode(Node n, Map<Node, FlowValue> nodeValues) {
            return null;
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationNode n,
                                          Map<Node, FlowValue> nodeValues) {
            List<Node> args = n.getArguments();
            if (args.isEmpty()) {
                return null;
            }

            QualifiedExecutableType<ProductSlot> methodType = (QualifiedExecutableType<ProductSlot>)
                    Objects.requireNonNull(nodeValues.get(n.getTarget())).type;
            List<QualifiedType<ProductSlot>> paramTypes = methodType.getParameterTypes();
            assert args.size() == paramTypes.size();

            for (int i = 0; i < args.size(); ++i) {
                Node argNode = args.get(i);
                QualifiedType<ProductSlot> argType = Objects.requireNonNull(nodeValues.get(argNode)).type;
                QualifiedType<ProductSlot> paramType = paramTypes.get(i);

                // TODO: handle vararg (both type check and location) correctly
                SetMultimap<QualifierHierarchy, Constraint> constraints =
                        typeHierarchy.getSubtypeConstraints(argType, paramType);
                AnalysisMessage unsatMessage = AnalysisMessage.createError(
                        generalContext.getRoot(),
                        NodeUtils.getRealSourceTree(flowContext, argNode),
                        "argument.type.incompatible",
                        argType,
                        paramType
                );
                constraintManager.addExplainedConstraints(constraints, unsatMessage);
            }
            return null;
        }

        @Override
        public Void visitAssignment(AssignmentNode n, Map<Node, FlowValue> nodeValues) {
            QualifiedType<ProductSlot> lhsType = nodeTypeResolver.getLhsNodeType(n.getTarget());
            QualifiedType<ProductSlot> rhsType = nodeValues.get(n.getExpression()).type;

            SetMultimap<QualifierHierarchy, Constraint> constraints =
                    typeHierarchy.getSubtypeConstraints(rhsType, lhsType);
            AnalysisMessage unsatMessage = AnalysisMessage.createError(
                    generalContext.getRoot(),
                    NodeUtils.getRealSourceTree(flowContext, n),
                    "assignment.type.incompatible",
                    rhsType,
                    lhsType
            );
            constraintManager.addExplainedConstraints(constraints, unsatMessage);
            return null;
        }

        @Override
        public Void visitReturn(ReturnNode n, Map<Node, FlowValue> nodeValues) {
            FlowValue resultValue = nodeValues.get(n);
            if (resultValue == null) {
                assert n.getResult() == null;
                return null;
            }

            ControlFlowGraph cfg = Objects.requireNonNull(flowContext.getCurrentCFG());
            UnderlyingAST underlyingAST = cfg.getUnderlyingAST();
            QualifiedType<ProductSlot> returnExprType = resultValue.type;

            if (underlyingAST.getKind() == UnderlyingAST.Kind.METHOD) {
                UnderlyingAST.CFGMethod method = (UnderlyingAST.CFGMethod) underlyingAST;
                ExecutableElement methodElement = TreeUtils.elementFromDeclaration(method.getMethod());
                QualifiedExecutableElement<ProductSlot> methodType = declarationTypeResolver.getType(methodElement);

                SetMultimap<QualifierHierarchy, Constraint> constraints =
                        typeHierarchy.getSubtypeConstraints(returnExprType, methodType.getReturnType());
                AnalysisMessage unsatMessage = AnalysisMessage.createError(
                        generalContext.getRoot(),
                        NodeUtils.getRealSourceTree(flowContext, n),
                        "return.type.incompatible",
                        returnExprType,
                        methodType.getReturnType()
                );
                constraintManager.addExplainedConstraints(constraints, unsatMessage);
            } else if (underlyingAST.getKind() == UnderlyingAST.Kind.LAMBDA) {
                // TODO
            } else {
                throw new PluginError("Unexpected underlying ast %s found with return node %s",
                        underlyingAST.getKind(),
                        n);
            }
            return null;
        }
    }
}
