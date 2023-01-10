package org.cfginference.core;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.event.EventManager;
import org.cfginference.core.model.reporting.PluginError;

import javax.lang.model.element.TypeElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class AbstractInferenceProcessor {

    protected final Context context;

    protected final EventManager eventManager;

    protected AbstractInferenceProcessor(Context context) {
        context.put((Class) this.getClass(), this);
        this.context = context;
        this.eventManager = EventManager.instance(context);
    }

    public static AbstractInferenceProcessor instance(Class<?> subclass, Context context) {
        Class<? extends AbstractInferenceProcessor> clazz = subclass.asSubclass(AbstractInferenceProcessor.class);
        AbstractInferenceProcessor processor = context.get(clazz);
        if (processor == null) {
            try {
                Constructor<? extends AbstractInferenceProcessor> constructor = clazz.getDeclaredConstructor(Context.class);
                constructor.setAccessible(true);
                processor = constructor.newInstance(context);
            } catch (NoSuchMethodException
                     | SecurityException
                     | InstantiationException
                     | IllegalAccessException
                     | IllegalArgumentException
                     | InvocationTargetException e) {
                throw new PluginError(e);
            }
        }
        return processor;
    }

    /**
     * A method to be called once before the first call to typeProcess.
     *
     * <p>Subclasses may override this method to do any initialization work.
     */
    public void preProcessing() {}

    /**
     * Processes a fully-analyzed class that contains a supported annotation.
     *
     * <p>The passed class is always valid type-checked Java code.
     *
     * @param element element of the analyzed class
     * @param path the tree path to the element, with the leaf being a {@link ClassTree}
     */
    public void process(TypeElement element, TreePath path) {
        // TODO: ensure only one processor in each session

    }

    /**
     * A method to be called once all the classes are processed.
     *
     * <p>Subclasses may override this method to do any aggregate analysis (e.g. generate report,
     * persistence) or resource deallocation.
     */
    public void postProcessing() {}
}
