package org.tainting;

import com.google.common.collect.ImmutableSet;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.typesystem.AbstractTypeSystem;
import org.cfginference.core.typesystem.BaseDeclarationTypeResolver;
import org.cfginference.core.typesystem.BaseNodeTypeResolver;
import org.cfginference.core.typesystem.BaseTypeChecker;
import org.cfginference.core.typesystem.BaseTypeSystemTransfer;
import org.cfginference.core.typesystem.DeclarationTypeResolver;
import org.cfginference.core.typesystem.NodeTypeResolver;
import org.cfginference.core.typesystem.QualifierHierarchy;
import org.cfginference.core.typesystem.TypeChecker;
import org.cfginference.core.typesystem.TypeSystem;
import org.cfginference.core.typesystem.TypeSystemTransfer;

import java.util.Set;

public class TaintingTypeSystem extends AbstractTypeSystem {

    public TaintingTypeSystem(Context context) {
        super(context, new TaintingQualifierHierarchy());
    }
}
