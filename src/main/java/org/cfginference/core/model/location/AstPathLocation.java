package org.cfginference.core.model.location;

import scenelib.annotations.io.ASTRecord;

import java.util.Objects;

public record AstPathLocation(ASTRecord astRecord) implements QualifierLocation {
    public AstPathLocation {
        Objects.requireNonNull(astRecord);
        // we don't retain any references to trees to prevent memory leaks
        assert astRecord.ast == null;
    }

    @Override
    public String getClassName() {
        return astRecord.className;
    }
}
