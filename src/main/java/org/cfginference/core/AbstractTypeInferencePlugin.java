package org.cfginference.core;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.util.Context;

import javax.lang.model.element.TypeElement;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractTypeInferencePlugin implements Plugin, TaskListener {

    @Parameter(names={"-h", "--help"},
            description="Help/Usage",
            help=true)
    private boolean help;

    protected Context context;

    protected JavacTask javacTask;

    /**
     * Method {@link #typeProcessingStart()} must be invoked exactly once, before any invocation of
     * {@link #typeProcess(TypeElement, TreePath)}.
     */
    private boolean hasInvokedTypeProcessingStart = false;

    /**
     * A method to be called once before the first call to typeProcess.
     *
     * <p>Subclasses may override this method to do any initialization work.
     */
    public void typeProcessingStart() {}

    /**
     * Processes a fully-analyzed class that contains a supported annotation.
     *
     * <p>The passed class is always valid type-checked Java code.
     *
     * @param element element of the analyzed class
     * @param tree the tree path to the element, with the leaf being a {@link ClassTree}
     */
    public abstract void typeProcess(TypeElement element, TreePath tree);

    /**
     * A method to be called once all the classes are processed.
     *
     * <p>Subclasses may override this method to do any aggregate analysis (e.g. generate report,
     * persistence) or resource deallocation.
     */
    public void typeProcessingOver() {}

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public final void init(JavacTask task, String... args) {
        Context context = ((BasicJavacTask) task).getContext();
        parseArgs(args);
        setContext(context);
        task.addTaskListener(this);
    }

    private void parseArgs(String... args) {
        JCommander.Builder builder = JCommander.newBuilder()
                .programName(getClass().getSimpleName());
        Set<Object> argumentObjects = getCLArgumentObjects();

        for (Object obj : argumentObjects) {
            builder.addObject(obj);
        }

        builder.build().parse(args);
    }

    protected Set<Object> getCLArgumentObjects() {
        return Set.of(this);
    }

    protected void setContext(Context context) {
        this.context = context;
    }

    @Override
    public final void finished(TaskEvent e) {
        switch(e.getKind()) {
            case ANALYZE:
                break;
            case COMPILATION:
                // compilation finished
                typeProcessingOver();
                return;
            default:
                return;
        }

        if (!hasInvokedTypeProcessingStart) {
            typeProcessingStart();
            hasInvokedTypeProcessingStart = true;
        }

        TypeElement element = e.getTypeElement();
        Objects.requireNonNull(element);
        Objects.requireNonNull(e.getCompilationUnit());
        TreePath path = Trees.instance(javacTask).getPath(element);

        typeProcess(element, path);
    }
}
