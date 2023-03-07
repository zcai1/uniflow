package org.uniflow.core.flow;

import com.google.common.collect.Sets;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import org.uniflow.core.PluginOptions;
import org.uniflow.core.model.reporting.PluginError;
import org.uniflow.core.typesystem.QualifierHierarchy;
import org.uniflow.util.AnnotationHelpers;
import org.uniflow.util.ProductSlotUtils;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
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
import org.plumelib.util.ToStringComparator;
import org.plumelib.util.UniqueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

public final class FlowStore implements Store<FlowStore>, UniqueId {

    private static final Logger logger = LoggerFactory.getLogger(FlowStore.class);

    private static final AtomicLong nextUid = new AtomicLong();
    private final long uid = nextUid.getAndIncrement();

    private final Context context;

    private final Types types;

    private final PluginOptions options;

    private @Nullable FlowValue thisValue;

    private final Map<LocalVariable, FlowValue> localVarValues;
    private final Map<FieldAccess, FlowValue> fieldValues;
    private final Map<ArrayAccess, FlowValue> arrayValues;
    private final Map<MethodCall, FlowValue> methodValues;
    private final Map<ClassName, FlowValue> classValues;

    public FlowStore(Context context) {
        this.context = context;
        this.types = JavacTypes.instance(context);
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
        types = other.types;
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
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FlowStore other)) {
            return false;
        }

        return Objects.equals(thisValue, other.thisValue)
                && localVarValues.equals(other.localVarValues)
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
    public String toString() {
        return this.getClassAndUid();
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
     * Remove any knowledge about the expression {@code forExpression} (correctly deciding where to remove
     * the information depending on the type of the expression {@code forExpression}).
     */
    public void clearValue(JavaExpression forExpression) {
        Map<? extends JavaExpression, FlowValue> valuesMap = getValuesMapByClass(forExpression.getClass());
        if (valuesMap != null) {
            valuesMap.remove(forExpression);
        } else if (forExpression instanceof ThisReference) {
            thisValue = null;
        }
    }

    public void clearValues(Class<? extends JavaExpression>... forExpressions) {
        for (Class<? extends JavaExpression> exprClass : forExpressions) {
            Map<? extends JavaExpression, FlowValue> valuesMap = getValuesMapByClass(exprClass);
            if (valuesMap != null) {
                valuesMap.clear();
            } else if (exprClass == ThisReference.class) {
                thisValue = null;
            }
        }
    }

    public @Nullable FlowValue getValue(JavaExpression expr) {
        Map<? extends JavaExpression, FlowValue> valuesMap = getValuesMapByClass(expr.getClass());
        if (valuesMap != null) {
            return valuesMap.get(expr);
        } else if (expr instanceof ThisReference) {
            return thisValue;
        } else {
            return null;
        }
    }

    public @Nullable FlowValue getValue(Node node) {
        JavaExpression expr = nodeToSupportExpression(node);
        if (expr != null) {
            return getValue(expr);
        }
        return null;
    }


    @Override
    public FlowStore widenedUpperBound(FlowStore previous) {
        // TODO: support widening
        return leastUpperBound(previous);
    }


    /**
     * Can the objects {@code a} and {@code b} be aliases? Returns a conservative answer (i.e.,
     * returns {@code true} if not enough information is available to determine aliasing).
     */
    @Override
    public boolean canAlias(JavaExpression a, JavaExpression b) {
        // TODO: may want to delegate this to type systems
        TypeMirror tb = b.getType();
        TypeMirror ta = a.getType();
        return types.isSubtype(ta, tb) || types.isSubtype(tb, ta);
    }

    @Override
    public String visualize(CFGVisualizer<?, FlowStore, ?> viz) {
        CFGVisualizer<FlowValue, FlowStore, ?> castedViz = (CFGVisualizer<FlowValue, FlowStore, ?>) viz;
        String internal = internalVisualize(castedViz);
        if (internal.trim().isEmpty()) {
            return this.getClassAndUid() + "()";
        } else {
            return this.getClassAndUid() + "(" + viz.getSeparator() + internal + ")";
        }
    }

    /**
     * Adds a representation of the internal information of this Store to visualizer {@code viz}.
     *
     * @param viz the visualizer
     * @return a representation of the internal information of this {@link Store}
     */
    private String internalVisualize(CFGVisualizer<FlowValue, FlowStore, ?> viz) {
        StringJoiner res = new StringJoiner(viz.getSeparator());
        for (LocalVariable lv : ToStringComparator.sorted(localVarValues.keySet())) {
            res.add(viz.visualizeStoreLocalVar(lv, localVarValues.get(lv)));
        }
        if (thisValue != null) {
            res.add(viz.visualizeStoreThisVal(thisValue));
        }
        for (FieldAccess fa : ToStringComparator.sorted(fieldValues.keySet())) {
            res.add(viz.visualizeStoreFieldVal(fa, fieldValues.get(fa)));
        }
        for (ArrayAccess fa : ToStringComparator.sorted(arrayValues.keySet())) {
            res.add(viz.visualizeStoreArrayVal(fa, arrayValues.get(fa)));
        }
        for (MethodCall fa : ToStringComparator.sorted(methodValues.keySet())) {
            res.add(viz.visualizeStoreMethodVals(fa, methodValues.get(fa)));
        }
        for (ClassName fa : ToStringComparator.sorted(classValues.keySet())) {
            res.add(viz.visualizeStoreClassVals(fa, classValues.get(fa)));
        }
        return res.toString();
    }

    @Override
    public long getUid() {
        return uid;
    }

    @EnsuresNonNullIf(expression = "#2", result = true)
    private boolean shouldInsert(JavaExpression expr, @Nullable FlowValue value, boolean permitNondeterministic) {
        if (value == null) {
            // No need to insert a null abstract value because it represents
            // top and top is also the default value.
            return false;
        }
        if (!canInsertJavaExpression(expr)) {
            // Expressions containing unknown expressions are not stored.
            return false;
        }
        // Nondeterministic expressions may not be stored.
        // (They are likely to be quickly evicted, as soon as a side-effecting method is
        // called.)
        // TODO: should we delegate this to type systems
        return permitNondeterministic || expr.isDeterministic(AnnotationHelpers.DEFAULT_ANNO_PROVIDER);
    }

    private void computeMergedValueAndInsert(
            JavaExpression expr,
            @Nullable FlowValue newValue,
            BiFunction<@Nullable FlowValue, FlowValue, @Nullable FlowValue> merger, // (old value, new value) -> merged
            boolean permitNondeterministic) {
        if (!shouldInsert(expr, newValue, permitNondeterministic)) {
            return;
        }

        Map<? extends JavaExpression, FlowValue> valuesMap = getValuesMapByClass(expr.getClass());
        FlowValue oldValue = null;
        if (valuesMap != null) {
            oldValue = valuesMap.get(expr);
        } else if (expr instanceof ThisReference) {
            oldValue = thisValue;
        }
        FlowValue mergedValue = merger.apply(oldValue, newValue);

        if (mergedValue == null) {
            return;
        }

        if (expr instanceof LocalVariable localVar) {
            assert valuesMap == localVarValues;
            localVarValues.put(localVar, mergedValue);
        } else if (expr instanceof FieldAccess fieldAccess) {
            assert valuesMap == fieldValues;
            // TODO: support monotonic update
            if (options.isSequentialSemantics() || fieldAccess.isUnassignableByOtherCode()) {
                fieldValues.put(fieldAccess, mergedValue);
            }
        } else if (expr instanceof MethodCall methodCall) {
            assert valuesMap == methodValues;
            if (options.isSequentialSemantics()) {
                methodValues.put(methodCall, mergedValue);
            }
        } else if (expr instanceof ArrayAccess arrayAccess) {
            assert valuesMap == arrayValues;
            if (options.isSequentialSemantics()) {
                arrayValues.put(arrayAccess, mergedValue);
            }
        } else if (expr instanceof ClassName className) {
            assert valuesMap == classValues;
            if (options.isSequentialSemantics() || className.isUnassignableByOtherCode()) {
                classValues.put(className, mergedValue);
            }
        } else if (expr instanceof ThisReference) {
            if (options.isSequentialSemantics()) {
                thisValue = mergedValue;
            }
        }
    }

    public void insertValue(JavaExpression expr, @Nullable FlowValue value) {
        insertValue(expr, value, false);
    }

    public void insertValue(Node node, @Nullable FlowValue value) {
        JavaExpression expr = nodeToSupportExpression(node);
        if (expr != null) {
            insertValue(expr, value, false);
        }
    }

    public void replaceValue(JavaExpression expr, @Nullable FlowValue value) {
        clearValue(expr);
        insertValue(expr, value);
    }

    public void replaceValue(Node node, @Nullable FlowValue value) {
        JavaExpression expr = nodeToSupportExpression(node);
        if (expr != null) {
            replaceValue(expr, value);
        }
    }

    public void insertValue(JavaExpression expr, @Nullable FlowValue value, boolean permitNondeterministic) {
        computeMergedValueAndInsert(
                expr,
                value,
                (oldValue, newValue) -> FlowValue.refine(oldValue, newValue,
                        ProductSlotUtils.IncomparableSlotResolver.ALWAYS_FIRST),
                permitNondeterministic
        );
    }

    public void updateForAssignment(Node lhs, @Nullable FlowValue value) {
        JavaExpression expr = nodeToSupportExpression(lhs);
        if (expr instanceof ArrayAccess arrayAccess) {
            removeConflicting(arrayAccess);
        } else if (expr instanceof LocalVariable localVariable) {
            removeConflicting(localVariable);
        } else if (expr instanceof FieldAccess fieldAccess) {
            removeConflicting(fieldAccess, value);
        } else {
            throw new PluginError("Unexpected lhs %s with value %s", lhs, value);
        }

        if (value != null) {
            replaceValue(expr, value);
        }
    }

    /**
     * Remove any information in this store that might not be true any more after {@code localVar}
     * has been assigned a new value. This includes the following steps:
     *
     * <ol>
     *   <li value="1">Remove any abstract values for field accesses <em>b.g</em> where {@code
     *       localVar} might alias any expression in the receiver <em>b</em>.
     *   <li value="2">Remove any abstract values for array accesses <em>a[i]</em> where {@code
     *       localVar} might alias the receiver <em>a</em>.
     *   <li value="3">Remove any information about method calls where the receiver or any of the
     *       parameters contains {@code localVar}.
     * </ol>
     */
    private void removeConflicting(LocalVariable var) {
        final Iterator<Map.Entry<FieldAccess, FlowValue>> fieldValuesIterator =
                fieldValues.entrySet().iterator();
        while (fieldValuesIterator.hasNext()) {
            Map.Entry<FieldAccess, FlowValue> entry = fieldValuesIterator.next();
            FieldAccess otherFieldAccess = entry.getKey();
            // case 1:
            if (otherFieldAccess.containsSyntacticEqualJavaExpression(var)) {
                fieldValuesIterator.remove();
            }
        }

        final Iterator<Map.Entry<ArrayAccess, FlowValue>> arrayValuesIterator =
                arrayValues.entrySet().iterator();
        while (arrayValuesIterator.hasNext()) {
            Map.Entry<ArrayAccess, FlowValue> entry = arrayValuesIterator.next();
            ArrayAccess otherArrayAccess = entry.getKey();
            // case 2:
            if (otherArrayAccess.containsSyntacticEqualJavaExpression(var)) {
                arrayValuesIterator.remove();
            }
        }

        final Iterator<Map.Entry<MethodCall, FlowValue>> methodValuesIterator =
                methodValues.entrySet().iterator();
        while (methodValuesIterator.hasNext()) {
            Map.Entry<MethodCall, FlowValue> entry = methodValuesIterator.next();
            MethodCall otherMethodAccess = entry.getKey();
            // case 3:
            if (otherMethodAccess.containsSyntacticEqualJavaExpression(var)) {
                methodValuesIterator.remove();
            }
        }
    }

    /**
     * Remove any information in this store that might not be true any more after {@code
     * fieldAccess} has been assigned a new value (with the abstract value {@code val}). This
     * includes the following steps (assume that {@code fieldAccess} is of the form <em>a.f</em> for
     * some <em>a</em>.
     *
     * <ol>
     *   <li value="1">Update the abstract value of other field accesses <em>b.g</em> where the
     *       field is equal (that is, <em>f=g</em>), and the receiver <em>b</em> might alias the
     *       receiver of {@code fieldAccess}, <em>a</em>. This update will raise the abstract value
     *       for such field accesses to at least {@code val} (or the old value, if that was less
     *       precise). However, this is only necessary if the field <em>g</em> is not final.
     *   <li value="2">Remove any abstract values for field accesses <em>b.g</em> where {@code
     *       fieldAccess} might alias any expression in the receiver <em>b</em>.
     *   <li value="3">Remove any information about method calls.
     *   <li value="4">Remove any abstract values an array access <em>b[i]</em> where {@code
     *       fieldAccess} might alias any expression in the receiver <em>a</em> or index <em>i</em>.
     * </ol>
     *
     * @param val the abstract value of the value assigned to {@code n} (or {@code null} if the
     *     abstract value is not known).
     */
    private void removeConflicting(FieldAccess fieldAccess, @Nullable FlowValue val) {
        final Iterator<Map.Entry<FieldAccess, FlowValue>> fieldValuesIterator =
                fieldValues.entrySet().iterator();
        while (fieldValuesIterator.hasNext()) {
            Map.Entry<FieldAccess, FlowValue> entry = fieldValuesIterator.next();
            FieldAccess otherFieldAccess = entry.getKey();
            FlowValue otherVal = entry.getValue();
            // case 2:
            if (otherFieldAccess.getReceiver().containsModifiableAliasOf(this, fieldAccess)) {
                fieldValuesIterator.remove(); // remove information completely
            }
            // case 1:
            else if (fieldAccess.getField().equals(otherFieldAccess.getField())) {
                if (canAlias(fieldAccess.getReceiver(), otherFieldAccess.getReceiver())) {
                    if (!otherFieldAccess.isFinal()) {
                        if (val != null) {
                            FlowValue newVal = val.leastUpperBound(otherVal);
                            entry.setValue(newVal);
                        } else {
                            // remove information completely
                            fieldValuesIterator.remove();
                        }
                    }
                }
            }
        }

        final Iterator<Map.Entry<ArrayAccess, FlowValue>> arrayValuesIterator =
                arrayValues.entrySet().iterator();
        while (arrayValuesIterator.hasNext()) {
            Map.Entry<ArrayAccess, FlowValue> entry = arrayValuesIterator.next();
            ArrayAccess otherArrayAccess = entry.getKey();
            if (otherArrayAccess.containsModifiableAliasOf(this, fieldAccess)) {
                // remove information completely
                arrayValuesIterator.remove();
            }
        }

        // case 3:
        methodValues.clear();
    }

    /**
     * Remove any information in the store that might not be true any more after {@code arrayAccess}
     * has been assigned a new value (with the abstract value {@code val}). This includes the
     * following steps (assume that {@code arrayAccess} is of the form <em>a[i]</em> for some
     * <em>a</em>.
     *
     * <ol>
     *   <li value="1">Remove any abstract value for other array access <em>b[j]</em> where
     *       <em>a</em> and <em>b</em> can be aliases, or where either <em>b</em> or <em>j</em>
     *       contains a modifiable alias of <em>a[i]</em>.
     *   <li value="2">Remove any abstract values for field accesses <em>b.g</em> where
     *       <em>a[i]</em> might alias any expression in the receiver <em>b</em> and there is an
     *       array expression somewhere in the receiver.
     *   <li value="3">Remove any information about method calls.
     * </ol>
     */
    private void removeConflicting(ArrayAccess arrayAccess) {
        final Iterator<Map.Entry<ArrayAccess, FlowValue>> arrayValuesIterator =
                arrayValues.entrySet().iterator();
        while (arrayValuesIterator.hasNext()) {
            Map.Entry<ArrayAccess, FlowValue> entry = arrayValuesIterator.next();
            ArrayAccess otherArrayAccess = entry.getKey();
            // case 1:
            if (otherArrayAccess.containsModifiableAliasOf(this, arrayAccess)) {
                arrayValuesIterator.remove(); // remove information completely
            } else if (canAlias(arrayAccess.getArray(), otherArrayAccess.getArray())) {
                // TODO: one could be less strict here, and only raise the abstract
                // value for all array expressions with potentially aliasing receivers.
                arrayValuesIterator.remove(); // remove information completely
            }
        }

        // case 2:
        final Iterator<Map.Entry<FieldAccess, FlowValue>> fieldValuesIterator =
                fieldValues.entrySet().iterator();
        while (fieldValuesIterator.hasNext()) {
            Map.Entry<FieldAccess, FlowValue> entry = fieldValuesIterator.next();
            FieldAccess otherFieldAccess = entry.getKey();
            JavaExpression otherReceiver = otherFieldAccess.getReceiver();
            if (otherReceiver.containsModifiableAliasOf(this, arrayAccess)
                    && otherReceiver.containsOfClass(ArrayAccess.class)) {
                // remove information completely
                fieldValuesIterator.remove();
            }
        }

        // case 3:
        methodValues.clear();
    }

    @SuppressWarnings("unchecked")
    private <T extends JavaExpression> @Nullable Map<T, FlowValue> getValuesMapByClass(Class<T> clazz) {
        if (clazz == LocalVariable.class) {
            return (Map<T, FlowValue>) localVarValues;
        } else if (clazz == FieldAccess.class) {
            return (Map<T, FlowValue>) fieldValues;
        } else if (clazz == MethodCall.class) {
            return (Map<T, FlowValue>) methodValues;
        } else if (clazz == ArrayAccess.class) {
            return (Map<T, FlowValue>) arrayValues;
        } else if (clazz == ClassName.class) {
            return (Map<T, FlowValue>) classValues;
        }
        return null;
    }

    /** Returns true if {@code expr} can be stored in this store. */
    @EnsuresNonNullIf(expression = "#1", result = true)
    public static boolean canInsertJavaExpression(@Nullable JavaExpression expr) {
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

    public static @Nullable JavaExpression nodeToSupportExpression(@Nullable Node node) {
        if (node instanceof MethodInvocationNode method) {
            return JavaExpression.fromNode(method);
        } else if (node instanceof ArrayAccessNode array) {
            return JavaExpression.fromArrayAccess(array);
        } else if (node instanceof LocalVariableNode local) {
            return new LocalVariable(local);
        } else if (node instanceof ThisNode thisNode) {
            return new ThisReference(thisNode.getType());
        } else if (node instanceof FieldAccessNode field) {
            JavaExpression je = JavaExpression.fromNodeFieldAccess(field);
            if (je instanceof ThisReference) {
                // "return thisValue" is wrong, because the node refers to an outer this.
                // TODO: A possible way to fix this is having a map from class element to the corresponding "this"
                return null;
            }
            return je;
        } else {
            return null;
        }
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
