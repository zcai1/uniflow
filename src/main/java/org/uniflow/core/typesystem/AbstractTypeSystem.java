package org.uniflow.core.typesystem;

import com.google.common.collect.ImmutableSet;
import com.sun.tools.javac.util.Context;

import java.util.Set;

public abstract class AbstractTypeSystem implements TypeSystem {

    protected final Context context;

    private final Set<QualifierHierarchy> qualifierHierarchies;

    private DeclarationTypeResolver declarationTypeResolver;

    private TypeSystemTransfer transfer;

    private TypeChecker typeChecker;

    private NodeTypeResolver nodeTypeResolver;

    private TypeHierarchy typeHierarchy;

    public AbstractTypeSystem(Context context, QualifierHierarchy hierarchy, QualifierHierarchy... moreHierarchies) {
        this.context = context;
        this.qualifierHierarchies = ImmutableSet.<QualifierHierarchy>builder()
                .add(hierarchy)
                .add(moreHierarchies)
                .build();
    }

    @Override
    public TypeHierarchy getTypeHierarchy() {
        if (typeHierarchy == null) {
            typeHierarchy = new BaseTypeHierarchy(context, this);
        }
        return typeHierarchy;
    }

    @Override
    public TypeSystemTransfer getTransferFunction() {
        if (transfer == null) {
            transfer = new BaseTypeSystemTransfer(context, this);
        }
        return transfer;
    }

    @Override
    public DeclarationTypeResolver getDeclarationTypeResolver() {
        if (declarationTypeResolver == null) {
            declarationTypeResolver = new BaseDeclarationTypeResolver(context, this);
        }
        return declarationTypeResolver;
    }

    @Override
    public TypeChecker getTypeChecker() {
        if (typeChecker == null) {
            typeChecker = new BaseTypeChecker(context, this);
        }
        return typeChecker;
    }

    @Override
    public NodeTypeResolver getNodeTypeResolver() {
        if (nodeTypeResolver == null) {
            nodeTypeResolver = new BaseNodeTypeResolver(context, this);
        }
        return nodeTypeResolver;
    }

    @Override
    public Set<QualifierHierarchy> getQualifierHierarchies() {
        return qualifierHierarchies;
    }
}
