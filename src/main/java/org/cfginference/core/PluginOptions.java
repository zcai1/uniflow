package org.cfginference.core;

import ch.qos.logback.classic.Level;
import com.beust.jcommander.Parameter;
import com.sun.tools.javac.util.Context;

import java.util.ArrayList;
import java.util.List;

public final class PluginOptions {

    public enum Mode {
        TYPE_CHECK, INFERENCE
    }

    public enum LogLevel {
        DEBUG(0), INFO(1), WARN(2), ERROR(3);

        private final int severity;

        LogLevel(int severity) {
            this.severity = severity;
        }

        public Level toLogbackLevel() {
            return switch (this) {
                case DEBUG -> Level.DEBUG;
                case INFO -> Level.INFO;
                case WARN -> Level.WARN;
                case ERROR -> Level.ERROR;
            };
        }

        public boolean allows(LogLevel targetLevel) {
            return this.severity <= targetLevel.severity;
        }
    }

    @Parameter(names={"-h", "--help"},
            description="Help/Usage",
            help=true)
    private boolean help;

    // TODO: deprecate this
    @Parameter(names={"-p", "--processor"},
            description="Inference Processor",
            required = true)
    private String processor;

    @Parameter(names={"-ts", "--type-system"},
            description="Set type systems to run.",
            variableArity = true)
    private List<TypeSystems.Alias> typeSystems = new ArrayList<>();

    @Parameter(names={"--cache-size"},
            description="Set internal cache size")
    private int cacheSize = 300;

    @Parameter(names={"--log-level"},
            description="Set log level")
    private LogLevel logLevel = LogLevel.INFO;

    @Parameter(names={"-m", "--mode"},
            description="Plugin mode")
    private Mode mode = Mode.INFERENCE;

    @Parameter(names={"-ae", "--assertionEnabled"},
            description="Whether process assertions in the source code or not")
    private boolean assertionEnabled = false;

    @Parameter(names={"-seq", "--sequentialSemantics"},
            description="Whether to assume a single-threaded runtime")
    private boolean sequentialSemantics = true;

    private PluginOptions(Context context) {
        context.put(PluginOptions.class, this);
    }

    public static PluginOptions instance(Context context) {
        PluginOptions options = context.get(PluginOptions.class);
        if (options == null) {
            options = new PluginOptions(context);
        }
        return options;
    }

    public boolean isHelp() {
        return help;
    }

    @Deprecated
    public String getProcessor() {
        return processor;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isAssertionEnabled() {
        return assertionEnabled;
    }

    public boolean isSequentialSemantics() {
        return sequentialSemantics;
    }

    public List<TypeSystems.Alias> getTypeSystems() {
        return typeSystems;
    }

    public int getCacheSize() {
        return cacheSize;
    }
}
