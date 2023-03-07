package org.uniflow.core.model.reporting;

import com.google.common.base.Preconditions;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

public class PluginError extends RuntimeException {

    public PluginError(String message) {
        this(message, new Throwable());
    }

    public PluginError(String message, @Nullable Object... args) {
        this(message.formatted(args), new Throwable());
    }

    public PluginError(Throwable cause) {
        this(Optional.ofNullable(cause.getMessage()).orElse(""), cause);
    }

    public PluginError(String message, Throwable cause) {
        super(message, cause);
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(cause);
    }
}
