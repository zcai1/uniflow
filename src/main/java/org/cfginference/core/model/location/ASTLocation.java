package org.cfginference.core.model.location;

import com.google.auto.value.AutoValue;
import scenelib.annotations.io.ASTRecord;

@AutoValue
public abstract class ASTLocation implements QualifierLocation {

    public abstract ASTRecord getASTRecord();
}
