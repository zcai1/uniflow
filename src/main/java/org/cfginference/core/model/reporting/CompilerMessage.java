package org.cfginference.core.model.reporting;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.sun.source.tree.Tree;
import org.checkerframework.checker.compilermsgs.qual.CompilerMessageKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

@AutoValue
public abstract class CompilerMessage {

    // element, tree, or null
    public abstract @Nullable Object getSource();

    public abstract Diagnostic.Kind getKind();

    public abstract @CompilerMessageKey String getMessageKey();

    // TODO: How to handle ProductSlots? We need to know the exact qualifiers.
    public abstract ImmutableList<Object> getArgs();

    public static CompilerMessage create(@Nullable Object source,
                                         Diagnostic.Kind kind,
                                         @CompilerMessageKey String messageKey,
                                         Object... args) {
        Preconditions.checkArgument(source == null || source instanceof Element || source instanceof Tree);
        return new AutoValue_CompilerMessage(source, kind, messageKey, ImmutableList.copyOf(args));
    }

    public static CompilerMessage createError(@Nullable Object source,
                                              @CompilerMessageKey String messageKey,
                                              Object... args) {
        return create(source, Diagnostic.Kind.ERROR, messageKey, args);
    }

    public static CompilerMessage createWarning(@Nullable Object source,
                                                @CompilerMessageKey String messageKey,
                                                Object... args) {
        return create(source, Diagnostic.Kind.WARNING, messageKey, args);
    }
}
