package org.cfginference.core.model.location;

import com.google.auto.value.AutoValue;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.cfg.node.Node;
import scenelib.annotations.io.ASTRecord;

@AutoValue
public abstract class NodeLocation implements QualifierLocation {

    public abstract Class<? extends Node> getNodeClass();

    public abstract String getNodeString();

    public abstract @Nullable ASTRecord getASTRecord();

    @Override
    public boolean isInsertable() {
        return false;
    }
}
