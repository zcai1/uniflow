package org.cfginference.core.typesystem;

import java.util.Set;

public interface TypeSystem {

    TypeSystemTransfer getTransferFunction();

    Set<QualifierHierarchy> getQualifierHierarchies();

    void getTypeChecker();
}
