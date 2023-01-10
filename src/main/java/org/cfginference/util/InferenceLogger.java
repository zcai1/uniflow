package org.cfginference.util;

import com.sun.tools.javac.util.Context;

public final class InferenceLogger {

    private InferenceLogger(Context context) {
        context.put(InferenceLogger.class, this);
    }

    public static InferenceLogger instance(Context context) {
        InferenceLogger logger = context.get(InferenceLogger.class);
        if (logger == null) {
            logger = new InferenceLogger(context);
        }
        return logger;
    }
}
