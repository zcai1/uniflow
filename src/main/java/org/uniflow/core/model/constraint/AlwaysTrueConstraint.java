package org.uniflow.core.model.constraint;

public final class AlwaysTrueConstraint extends ConstantConstraint {

    private static final AlwaysTrueConstraint instance = new AlwaysTrueConstraint();

    public static AlwaysTrueConstraint instance() {
        return instance;
    }

    private AlwaysTrueConstraint() {}
}
