package org.cfginference.core.flow;

import com.google.common.collect.Sets;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.PluginOptions;
import org.cfginference.core.model.error.PluginError;
import org.cfginference.core.typesystem.QualifierHierarchy;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.analysis.Store;
import org.checkerframework.dataflow.cfg.node.ArrayAccessNode;
import org.checkerframework.dataflow.cfg.node.FieldAccessNode;
import org.checkerframework.dataflow.cfg.node.LocalVariableNode;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.dataflow.cfg.node.ThisNode;
import org.checkerframework.dataflow.cfg.visualize.CFGVisualizer;
import org.checkerframework.dataflow.expression.ArrayAccess;
import org.checkerframework.dataflow.expression.ClassName;
import org.checkerframework.dataflow.expression.FieldAccess;
import org.checkerframework.dataflow.expression.JavaExpression;
import org.checkerframework.dataflow.expression.LocalVariable;
import org.checkerframework.dataflow.expression.MethodCall;
import org.checkerframework.dataflow.expression.ThisReference;
import org.plumelib.util.UniqueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public final class FlowStore implements Store<FlowStore>, UniqueId {

    private static final Logger logger = LoggerFactory.getLogger(FlowStore.class);

    private static final AtomicLong nextUid = new AtomicLong();
    private final long uid = nextUid.getAndIncrement();

    private final Context context;

    private final PluginOptions options;

    @Nullable
    private FlowValue thisValue;

    private final Map<LocalVariable, FlowValue> localVarValues;
    private final Map<FieldAccess, FlowValue> fieldValues;
    private final Map<ArrayAccess, FlowValue> arrayValues;
    private final Map<MethodCall, FlowValue> methodValues;
    private final Map<ClassName, FlowValue> classValues;

    public FlowStore(Context context) {
        this.context = context;
        this.options = PluginOptions.instance(context);
        this.thisValue = null;
        this.localVarValues = new LinkedHashMap<>();
        this.fieldValues = new LinkedHashMap<>();
        this.arrayValues = new LinkedHashMap<>();
        this.methodValues = new LinkedHashMap<>();
        this.classValues = new LinkedHashMap<>();
    }

    public FlowStore(FlowStore other) {
        context = other.context;
        options = other.options;
        thisValue = other.thisValue;
        localVarValues = new LinkedHashMap<>(other.localVarValues);
        fieldValues = new LinkedHashMap<>(other.fieldValues);
        arrayValues = new LinkedHashMap<>(other.arrayValues);
        methodValues = new LinkedHashMap<>(other.methodValues);
        classValues = new LinkedHashMap<>(other.classValues);
    }

    @Override
    public FlowStore copy() {
        return new FlowStore(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FlowStore other)) {
            return false;
        }

        return Objects.equals(thisValue, other.thisValue)
                && fieldValues.equals(other.fieldValues)
                && arrayValues.equals(other.arrayValues)
                && methodValues.equals(other.methodValues)
                && classValues.equals(other.classValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localVarValues, fieldValues, arrayValues, methodValues, classValues);
    }

    @Override
    public FlowStore leastUpperBound(FlowStore other) {
        FlowStore newStore = new FlowStore(context);

        if (options.getLogLevel().allows(PluginOptions.LogLevel.DEBUG)) {
            debugLeastUpperBound(other);
        }

        if (thisValue != null && other.thisValue != null) {
            newStore.thisValue = thisValue.leastUpperBound(other.thisValue);
        }
        leastUpperBound(localVarValues, other.localVarValues, newStore.localVarValues);
        leastUpperBound(fieldValues, other.fieldValues, newStore.fieldValues);
        leastUpperBound(arrayValues, other.arrayValues, newStore.arrayValues);
        leastUpperBound(methodValues, other.methodValues, newStore.methodValues);
        leastUpperBound(classValues, other.classValues, newStore.classValues);
        return newStore;
    }

    public FlowStore replace(@Nullable FlowStore withStore, Set<QualifierHierarchy> forHierarchies, boolean inPlace) {
        if (this == withStore) {
            return this;
        }

        FlowStore returnStore = inPlace ? this : new FlowStore(context);

        if (withStore != null) {
            returnStore.thisValue = FlowValue.replace(thisValue, withStore.thisValue, forHierarchies);
            replace(localVarValues, withStore.localVarValues, returnStore.localVarValues, forHierarchies);
            replace(fieldValues, withStore.fieldValues, returnStore.fieldValues, forHierarchies);
            replace(arrayValues, withStore.arrayValues, returnStore.arrayValues, forHierarchies);
            replace(methodValues, withStore.methodValues, returnStore.methodValues, forHierarchies);
            replace(classValues, withStore.classValues, returnStore.classValues, forHierarchies);
        } else {
            returnStore.thisValue = FlowValue.replace(thisValue, null, forHierarchies);
            replace(localVarValues, Collections.emptyMap(), returnStore.localVarValues, forHierarchies);
            replace(fieldValues, Collections.emptyMap(), returnStore.fieldValues, forHierarchies);
            replace(arrayValues, Collections.emptyMap(), returnStore.arrayValues, forHierarchies);
            replace(methodValues, Collections.emptyMap(), returnStore.methodValues, forHierarchies);
            replace(classValues, Collections.emptyMap(), returnStore.classValues, forHierarchies);
        }
        return returnStore;
    }

    private void debugLeastUpperBound(FlowStore other) {
        if (thisValue != null || other.thisValue != null) {
            if (thisValue == null || other.thisValue == null) {
                logger.debug("leastUpperBound will ignore the merge of this values: {} and {}",
                        thisValue, other.thisValue);
            }
        }

        String debugMsg = "leastUpperBound will ignore the merge of {}";
        for (LocalVariable e : Sets.symmetricDifference(localVarValues.keySet(), other.localVarValues.keySet())) {
            logger.debug(debugMsg, e);
        }
        for (FieldAccess e : Sets.symmetricDifference(fieldValues.keySet(), other.fieldValues.keySet())) {
            logger.debug(debugMsg, e);
        }
        for (ArrayAccess e : Sets.symmetricDifference(arrayValues.keySet(), other.arrayValues.keySet())) {
            logger.debug(debugMsg, e);
        }
        for (MethodCall e : Sets.symmetricDifference(methodValues.keySet(), other.methodValues.keySet())) {
            logger.debug(debugMsg, e);
        }
        for (ClassName e : Sets.symmetricDifference(classValues.keySet(), other.classValues.keySet())) {
            logger.debug(debugMsg, e);
        }
    }

    /**
     * Remove any knowledge about the expression {@code expr} (correctly deciding where to remove
     * the information depending on the type of the expression {@code expr}).
     */
    public void clearValue(JavaExpression expr) {
        if (expr instanceof LocalVariable localVar) {
            localVarValues.remove(localVar);
        } else if (expr instanceof FieldAccess fieldAcc) {
            fieldValues.remove(fieldAcc);
        } else if (expr instanceof MethodCall method) {
            methodValues.remove(method);
        } else if (expr instanceof ArrayAccess a) {
            arrayValues.remove(a);
        } else if (expr instanceof ClassName c) {
            classValues.remove(c);
        } else if (expr instanceof ThisReference thisRef) {
            thisValue = null;
        }
    }

    public @Nullable FlowValue getValue(JavaExpression expr) {
        if (expr instanceof LocalVariable localVar) {
            return localVarValues.get(localVar);
        } else if (expr instanceof ThisReference) {
            return thisValue;
        } else if (expr instanceof FieldAccess fieldAcc) {
            return fieldValues.get(fieldAcc);
        } else if (expr instanceof MethodCall method) {
            return methodValues.get(method);
        } else if (expr instanceof ArrayAccess a) {
            return arrayValues.get(a);
        } else if (expr instanceof ClassName c) {
            return classValues.get(c);
        } else {
            throw new PluginError("Unexpected JavaExpression: %s (%s)", expr, expr.getClass());
        }
    }

    public @Nullable FlowValue getValue(Node node) {
        if (node instanceof MethodInvocationNode method) {
            return methodValues.get(JavaExpression.fromNode(method));
        } else if (node instanceof ArrayAccessNode array) {
            return arrayValues.get(JavaExpression.fromArrayAccess(array));
        } else if (node instanceof LocalVariableNode local) {
            return localVarValues.get(new LocalVariable(local));
        } else if (node instanceof ThisNode thisNode) {
            return thisValue;
        } else if (node instanceof FieldAccessNode field) {
            JavaExpression je = JavaExpression.fromNodeFieldAccess(field);
            if (je instanceof ThisReference) {
                // "return thisValue" is wrong, because the node refers to an outer this.
                // TODO: A possible way to fix this is having a map from class element to the corresponding "this"
                return null;
            }
            return getValue(je);
        } else {
            return null;
        }
    }


    @Override
    public FlowStore widenedUpperBound(FlowStore previous) {
        // TODO: support widening
        return leastUpperBound(previous);
    }

    @Override
    public boolean canAlias(JavaExpression a, JavaExpression b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String visualize(CFGVisualizer<?, FlowStore, ?> viz) {
        return null;
    }

    @Override
    public long getUid() {
        return uid;
    }

    /** Returns true if {@code expr} can be stored in this store. */
    public static boolean canInsertJavaExpression(JavaExpression expr) {
        if (expr instanceof FieldAccess
                || expr instanceof ThisReference
                || expr instanceof LocalVariable
                || expr instanceof MethodCall
                || expr instanceof ArrayAccess
                || expr instanceof ClassName) {
            return !expr.containsUnknown();
        }
        return false;
    }

    private static <T extends JavaExpression> void leastUpperBound(Map<T, FlowValue> src1,
                                                                   Map<T, FlowValue> src2,
                                                                   Map<T, FlowValue> target) {
        for (Map.Entry<T, FlowValue> e : src2.entrySet()) {
            T javaExp = e.getKey();
            FlowValue val1 = src1.get(javaExp);
            if (val1 != null) {
                FlowValue val2 = e.getValue();
                target.put(javaExp, val1.leastUpperBound(val2));
            }
        }
    }

    // public FlowStore combine(FlowStore other, boolean inPlace) {
    //     if (this == other) {
    //         return this;
    //     }
    //
    //     FlowStore returnStore = inPlace ? this : new FlowStore(context);
    //     returnStore.thisValue = FlowValue.combine(thisValue, other.thisValue);
    //     combine(localVarValues, other.localVarValues, returnStore.localVarValues);
    //     combine(fieldValues, other.fieldValues, returnStore.fieldValues);
    //     combine(arrayValues, other.arrayValues, returnStore.arrayValues);
    //     combine(methodValues, other.methodValues, returnStore.methodValues);
    //     combine(classValues, other.classValues, returnStore.classValues);
    //     return returnStore;
    // }
    //
    // private static <T extends JavaExpression> void combine(Map<T, FlowValue> src1,
    //                                                        Map<T, FlowValue> src2,
    //                                                        Map<T, FlowValue> target) {
    //     for (Map.Entry<T, FlowValue> e : src2.entrySet()) {
    //         T javaExp = e.getKey();
    //         FlowValue val1 = src1.get(javaExp);
    //         FlowValue val2 = e.getValue();
    //         target.put(javaExp, FlowValue.combine(val1, val2));
    //     }
    //     for (Map.Entry<T, FlowValue> e : src1.entrySet()) {
    //         T javaExp = e.getKey();
    //         if (!src2.containsKey(javaExp)) {
    //             target.put(javaExp, e.getValue());
    //         }
    //     }
    // }

    private static <T extends JavaExpression> void replace(Map<T, FlowValue> oldSrc,
                                                           Map<T, FlowValue> newSrc,
                                                           Map<T, FlowValue> target,
                                                           Set<QualifierHierarchy> forHierarchies) {
        for (Map.Entry<T, FlowValue> e : newSrc.entrySet()) {
            T javaExp = e.getKey();
            FlowValue oldValue = oldSrc.get(javaExp);
            FlowValue newValue = e.getValue();
            target.put(javaExp, FlowValue.replace(oldValue, newValue, forHierarchies));
        }

        for (Map.Entry<T, FlowValue> e : oldSrc.entrySet()) {
            T javaExp = e.getKey();
            if (!newSrc.containsKey(javaExp)) {
                target.put(javaExp, FlowValue.replace(e.getValue(), null, forHierarchies));
            }
        }
    }
}
