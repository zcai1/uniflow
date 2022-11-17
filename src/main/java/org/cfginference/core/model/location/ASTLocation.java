package org.cfginference.core.model.location;

import com.google.common.base.Preconditions;
import scenelib.annotations.io.ASTRecord;

public record ASTLocation(ASTRecord astRecord, boolean isInsertable) implements QualifierLocation {
    public ASTLocation {
        Preconditions.checkNotNull(astRecord);
        // we don't retain any references to trees to prevent memory leaks
        assert astRecord.ast == null;
    }
}
