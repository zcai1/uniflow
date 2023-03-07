package org.cfginference.core.flow;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.PluginOptions;
import org.cfginference.core.TypeSystems;
import org.cfginference.core.model.element.QualifiedElement;
import org.cfginference.core.model.slot.ProductSlot;
import org.cfginference.core.model.util.SlotLocator;
import org.cfginference.core.typesystem.TypeSystem;
import org.cfginference.util.ProductSlotUtils;
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public final class ASTAnalysisScanner extends TreePathScanner<Void, Void> {

    private final Context context;

    private final TypeSystems typeSystems;

    private final SlotLocator slotLocator;

    private final boolean inferenceMode;

    private ASTAnalysisScanner(Context context) {
        this.context = context;
        this.typeSystems = TypeSystems.instance(context);
        this.slotLocator = SlotLocator.instance(context);
        this.inferenceMode = PluginOptions.instance(context).getMode().isInference();

        context.put(ASTAnalysisScanner.class, this);
    }

    public static ASTAnalysisScanner instance(Context context) {
        ASTAnalysisScanner instance = context.get(ASTAnalysisScanner.class);
        if (instance == null) {
            instance = new ASTAnalysisScanner(context);
        }
        return instance;
    }

    @Override
    public Void visitClass(ClassTree tree, Void unused) {
        locateSourceSlots(tree);
        typeCheckDeclaration(tree);
        return super.visitClass(tree, unused);
    }

    @Override
    public Void visitMethod(MethodTree tree, Void unused) {
        locateSourceSlots(tree);
        typeCheckDeclaration(tree);
        return super.visitMethod(tree, unused);
    }

    @Override
    public Void visitVariable(VariableTree tree, Void unused) {
        VariableElement variableElement = TreeUtils.elementFromDeclaration(tree);
        if (variableElement.getKind() != ElementKind.PARAMETER) {
            // params should be handled by visitMethod
            locateSourceSlots(tree);
        }
        typeCheckDeclaration(tree);
        return super.visitVariable(tree, unused);
    }

    void handleArtificialTrees(Map<Tree, TreePath> treeToPath) {
        for (Map.Entry<Tree, TreePath> e : treeToPath.entrySet()) {
            Tree tree = e.getKey();
            if (tree.getKind() == Tree.Kind.VARIABLE) {
                locateSourceSlots(tree);
            }
        }
    }

    private void locateSourceSlots(Tree tree) {
        if (!inferenceMode) {
            return;
        }
        Element element = Objects.requireNonNull(TreeUtils.elementFromTree(tree));
        QualifiedElement<ProductSlot> declType = getCombinedQualifiedElement(element);
        slotLocator.locateSourceSlots(declType, tree);
    }

    private QualifiedElement<ProductSlot> getCombinedQualifiedElement(Element element) {
        Iterator<TypeSystem> typeSystemsIter = typeSystems.get().iterator();
        QualifiedElement<ProductSlot> result = null;

        while (typeSystemsIter.hasNext()) {
            QualifiedElement<ProductSlot> next = typeSystemsIter.next().getDeclarationTypeResolver().getType(element);
            result = (result == null) ? next : ProductSlotUtils.combine(context, result, next);
        }
        return Objects.requireNonNull(result);
    }

    private void typeCheckDeclaration(Tree tree) {
        Element element = Objects.requireNonNull(TreeUtils.elementFromTree(tree));
        for (TypeSystem typeSystem : typeSystems.get()) {
            typeSystem.getTypeChecker().checkDeclaration(element, tree);
        }
    }
}
