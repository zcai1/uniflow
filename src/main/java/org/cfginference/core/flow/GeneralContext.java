package org.cfginference.core.flow;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.util.Context;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class GeneralContext {

    private @Nullable CompilationUnitTree root;

    public static GeneralContext instance(Context context) {
        GeneralContext instance = context.get(GeneralContext.class);
        if (instance == null) {
            instance = new GeneralContext(context);
        }
        return instance;
    }

    private GeneralContext(Context context) {
        context.put(GeneralContext.class, this);
    }

    public CompilationUnitTree getRoot() {
        return Objects.requireNonNull(root);
    }

    public void setRoot(@Nullable CompilationUnitTree root) {
        this.root = root;
    }
}
