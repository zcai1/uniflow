package org.cfginference.core.solver.util;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class SolverOptions {

    /**
     * Map of configuration. Key is argument name, value is argument value.
     */
    private final Map<String, String> options;

    public SolverOptions(Map<String, String> options) {
        this.options = ImmutableMap.copyOf(options);
    }

    /**
     * Get the value for a given argument name.
     * @param arg the SolverArg of the given argument.
     * @return the string value for a given argument name.
     */
    public String getArg(SolverArg arg) {
        return options.get(arg.name());
    }

    /**
     * Get the boolean value for a given argument name.
     *
     * @param arg the SolverArg of the given argument.
     * @return true if the lower case of the string value of this argument equals to "true",
     * otherwise return false.
     */
    public boolean getBoolArg(SolverArg arg) {
        String argValue = options.get(arg.name());
        return argValue != null && argValue.equalsIgnoreCase("true");
    }
}
