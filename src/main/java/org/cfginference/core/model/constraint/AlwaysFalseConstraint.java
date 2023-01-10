package org.cfginference.core.model.constraint;

public final class AlwaysFalseConstraint extends ConstantConstraint {

    private static final AlwaysFalseConstraint instance = new AlwaysFalseConstraint();

    public static AlwaysFalseConstraint instance() {
        return instance;
    }

    private AlwaysFalseConstraint() {}
}
