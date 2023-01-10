package org.cfginference.core;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.auto.service.AutoService;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.comp.CompileStates;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.event.Event;
import org.cfginference.core.event.EventManager;
import org.cfginference.core.flow.EnterAnalysis;
import org.cfginference.core.model.reporting.PluginError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.TypeElement;

@AutoService(Plugin.class)
@SuppressWarnings("initialization.field.uninitialized")
public final class CFGInferencePlugin implements Plugin, TaskListener {

    public static final Logger logger = LoggerFactory.getLogger(CFGInferencePlugin.class);

    private static final String PROGRAM_NAME = "CFGInf";

    private Context context;

    private JavacTask javacTask;

    private PluginOptions options;

    private EventManager eventManager;

    private EnterAnalysis entryPoint;

    /**
     * Whether this {@link TaskListener} has received a {@link TaskEvent.Kind#ANALYZE} event
     */
    private boolean hasProcessingStarted = false;

    @Override
    public String getName() {
        return PROGRAM_NAME;
    }

    @Override
    public void init(JavacTask task, String... args) {
        javacTask = task;
        context = ((BasicJavacTask) task).getContext();
        options = PluginOptions.instance(context);
        eventManager = EventManager.instance(context);

        // should at least run dataflow analysis
        JavaCompiler compiler = JavaCompiler.instance(context);
        compiler.shouldStopPolicyIfNoError =
                CompileStates.CompileState.max(compiler.shouldStopPolicyIfNoError, CompileStates.CompileState.FLOW);
        compiler.shouldStopPolicyIfError =
                CompileStates.CompileState.max(compiler.shouldStopPolicyIfError, CompileStates.CompileState.FLOW);

        JCommander jCommander = JCommander.newBuilder()
                .programName(PROGRAM_NAME)
                .addObject(options)
                .build();
        try {
            jCommander.parse(args);
        } catch (ParameterException e) {
            logger.error("Failed to parse options. Use option -h or --help for help/usage details.");
            throw e;
        }

        if (options.isHelp()) {
            jCommander.usage();
        } else {
            validateOptions(options);
            setLogLevel(options.getLogLevel());
            entryPoint = EnterAnalysis.instance(context);
            task.addTaskListener(this);
        }
    }

    public static void setLogLevel(PluginOptions.LogLevel level) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
                ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(level.toLogbackLevel());
    }

    private static AbstractInferenceProcessor initProcessor(PluginOptions options, Context context) {
        Preconditions.checkNotNull(options);
        try {
            Class<?> processorClass = Class.forName(options.getProcessor());
            return AbstractInferenceProcessor.instance(processorClass, context);
        } catch (ClassNotFoundException e) {
            throw new PluginError(e);
        }
    }

    @Override
    public void finished(TaskEvent e) {
        if (e.getKind() == TaskEvent.Kind.COMPILATION) {
            // compilation finished
            eventManager.broadcast(Event.SimpleEvent.FULL_ANALYSIS, false);
            return;
        } else if (e.getKind() != TaskEvent.Kind.ANALYZE) {
            return;
        }

        if (!hasProcessingStarted) {
            eventManager.broadcast(Event.SimpleEvent.FULL_ANALYSIS, true);
            hasProcessingStarted = true;
        }

        TypeElement element = e.getTypeElement();
        Verify.verifyNotNull(element);
        Verify.verifyNotNull(e.getCompilationUnit());
        TreePath path = Trees.instance(javacTask).getPath(element);

        entryPoint.enter(path);
    }

    private static void validateOptions(PluginOptions options) {
        Verify.verify(options.getCacheSize() >= PluginOptions.CACHE_SIZE_MIN,
                "Minimum cache size is %s",
                PluginOptions.CACHE_SIZE_MIN);

        Verify.verify(options.getFlowDotDir() == null || !options.getFlowDotDir().isEmpty(),
                "Flowdotdir should never be empty");
    }
}
