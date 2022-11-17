package org.cfginference.core.model.error;

import com.google.common.base.Preconditions;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PluginError extends RuntimeException {

    public PluginError(String message) {
        this(message, new Throwable());
    }

    public PluginError(String message, @Nullable Object... args) {
        this(message.formatted(args), new Throwable());
    }

    public PluginError(Throwable cause) {
        this(cause.getMessage(), new Throwable());
    }

    public PluginError(String message, Throwable cause) {
        super(message, cause);
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(cause);
    }
}
