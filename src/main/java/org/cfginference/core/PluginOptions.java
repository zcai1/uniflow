package org.cfginference.core;

import ch.qos.logback.classic.Level;
import com.sun.tools.javac.util.Context;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.EnumSet;
import java.util.Set;

@SuppressWarnings("initialization")
@Command(name = "-Xplugin:<plugin name>",
        sortOptions = false)
public final class PluginOptions {

    public static final int CACHE_SIZE_MIN = 300;

    public enum Mode {
        TYPE_CHECK, INFERENCE;

        public boolean isInference() {
            return this == INFERENCE;
        }
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

    @Option(names = {"-h", "--help"}, help = true, description = "Help/Usage")
    private boolean help;

    @Option(names = {"-m", "--mode"},
            description = "Set plugin mode (default: ${DEFAULT-VALUE}). Candidates: ${COMPLETION-CANDIDATES}")
    private Mode mode = Mode.INFERENCE;

    @Option(names = {"-ts", "--type-systems"},
        split = ",",
        arity = "1..*",
        description = "Set type systems to run (separated by comma). " +
                "Candidates: ${COMPLETION-CANDIDATES}")
    private EnumSet<TypeSystems.Name> typeSystems;

    @Option(names = {"--jaif-output-dir", "-jaifo"},
            description = "Path to output jaif file (default: ${DEFAULT-VALUE}).")
    private String jaifOutputPath = "inference.jaif";

    // Semantics Options
    @Option(names = {"-ae", "--assertion-enabled"},
            description = "Whether process assertions in the source code or not (default: ${DEFAULT-VALUE}).")
    private boolean assertionEnabled = false;

    @Option(names = {"-seq", "--sequential-semantics"},
            description = "Whether to assume a single-threaded runtime (default: ${DEFAULT-VALUE}).")
    private boolean sequentialSemantics = true;

    @Option(names = {"--invariant-array"},
            description = "Should make array component types invariant (default: ${DEFAULT-VALUE}).")
    private boolean invariantArrays = false;

    // CFG visualization options
    @Option(names = {"--flowoutdir"},
            description = "Directory to place CFG and type resolution visualization.")
    private String flowOutDir;

    @Option(names = {"--verbose-cfg"},
            description = "Should append verbose information to CFG visualization (default: ${DEFAULT-VALUE}).")
    private boolean verboseCfg = false;

    // Annotation File Utilities options
    @Option(names = {"--afu-scripts-path", "-afup"},
            description = "Path to AFU scripts directory.")
    private String pathToAfuScripts;

    @Option(names = {"--afu-output-dir", "-afud"},
            description = "Annotation file utilities output directory.  WARNING: This directory must be empty.")
    private String afuOutputDir;

    // Misc options
    @Option(names = {"--log-level"},
            description = "Set log level (default: ${DEFAULT-VALUE}). Candidates: ${COMPLETION-CANDIDATES}")
    private LogLevel logLevel = LogLevel.INFO;

    @Option(names = {"--cache-size"},
            description = "Set internal cache size (default: ${DEFAULT-VALUE}).")
    private int cacheSize = CACHE_SIZE_MIN;

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

    public Set<TypeSystems.Name> getTypeSystems() {
        return typeSystems;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public String getFlowOutDir() {
        return flowOutDir;
    }

    public boolean isVerboseCfg() {
        return verboseCfg;
    }

    public boolean isInvariantArrays() {
        return invariantArrays;
    }

    public String getPathToAfuScripts() {
        return pathToAfuScripts;
    }

    public String getAfuOutputDir() {
        return afuOutputDir;
    }

    public String getJaifOutputPath() {
        return jaifOutputPath;
    }
}
