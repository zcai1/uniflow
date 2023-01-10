package org.cfginference.core;

import com.google.common.base.Verify;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.typesystem.TypeSystem;
import org.tainting.TaintingTypeSystem;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class TypeSystems {

    public enum Name {
        TAINTING;
    }

    private static Map<Name, Function<Context, TypeSystem>> initializers = new EnumMap<>(Name.class);

    static {
        initializers.put(Name.TAINTING, TaintingTypeSystem::new);
    }

    private final Set<TypeSystem> typeSystems;

    public static TypeSystems instance(Context context) {
        TypeSystems instance = context.get(TypeSystems.class);
        if (instance == null) {
            instance = new TypeSystems(context);
        }
        return instance;
    }

    private TypeSystems(Context context) {
        Set<TypeSystem> instances = new LinkedHashSet<>();
        PluginOptions options = PluginOptions.instance(context);
        for (Name name : options.getTypeSystems()) {
            Function<Context, TypeSystem> typeSystemInitializer = initializers.get(name);
            Verify.verifyNotNull(typeSystemInitializer, "Failed to find initializer for %s", name);
            instances.add(typeSystemInitializer.apply(context));
        }
        typeSystems = Collections.unmodifiableSet(instances);

        context.put(TypeSystems.class, this);
    }

    public Set<TypeSystem> get() {
        return typeSystems;
    }
}
