package org.cfginference.core;

import com.sun.tools.javac.util.Context;
import org.cfginference.core.typesystem.TypeSystem;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class TypeSystems {

    public enum Alias {
        NULLNESS, VALUE, UNIT // TODO: correct the enums
    }

    private static Map<Alias, Function<Context, TypeSystem>> initializers = new EnumMap<>(Alias.class);

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
        for (Alias alias : options.getTypeSystems()) {
            instances.add(initializers.get(alias).apply(context));
        }
        typeSystems = Collections.unmodifiableSet(instances);

        context.put(TypeSystems.class, this);
    }

    public Set<TypeSystem> get() {
        return typeSystems;
    }
}
