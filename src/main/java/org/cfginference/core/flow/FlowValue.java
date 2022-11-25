package org.cfginference.core.flow;

import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.model.error.PluginError;
import org.cfginference.core.model.slot.ProductSlot;
import org.cfginference.core.model.type.QualifiedType;
import org.cfginference.core.model.util.TypeQualifierComparator;
import org.cfginference.core.typesystem.QualifierHierarchy;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.dataflow.analysis.AbstractValue;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Objects;
import java.util.Set;

public final class FlowValue implements AbstractValue<FlowValue> {

    private static final TypeQualifierComparator<ProductSlot> qualifierComparator = new TypeQualifierComparator<>();

    public final QualifiedType<ProductSlot> type;

    private final Context context;

    private final Types types;

    public FlowValue(Context context, QualifiedType<ProductSlot> type) {
        this.context = context;
        this.type = type;
        this.types = JavacTypes.instance(context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlowValue flowValue)) return false;

        TypeMirror thisJavaType = type.getJavaType();
        TypeMirror otherJavaType = flowValue.type.getJavaType();
        return (thisJavaType == otherJavaType || types.isSameType(thisJavaType, otherJavaType))
                && qualifierComparator.scan(type, flowValue.type);
    }
    
    private boolean fastEquals(@Nullable Object o) {
        return this == o || (o instanceof FlowValue other && this.type == other.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return "FlowValue{" +
                "type=" + type +
                '}';
    }

    @Override
    public FlowValue leastUpperBound(FlowValue other) {
        if (this.fastEquals(other)) {
            return this;
        }

        TypeMirror thisJavaType = type.getJavaType();
        TypeMirror otherJavaType = other.type.getJavaType();
        if (!types.isSameType(thisJavaType, otherJavaType)) {
            throw new PluginError("Merging %s and %s is not supported", thisJavaType, otherJavaType);
        }

        QualifiedType<ProductSlot> lubType = ProductSlotUtils.merge(context, this.type, other.type, true);
        return new FlowValue(context, lubType);
    }

    public FlowValue greatestLowerBound(FlowValue other) {
        if (this.fastEquals(other)) {
            return this;
        }

        TypeMirror thisJavaType = type.getJavaType();
        TypeMirror otherJavaType = other.type.getJavaType();
        if (!types.isSameType(thisJavaType, otherJavaType)) {
            throw new PluginError("Merging %s and %s is not supported", thisJavaType, otherJavaType);
        }

        QualifiedType<ProductSlot> glbType = ProductSlotUtils.merge(context, this.type, other.type, false);
        return new FlowValue(context, glbType);
    }

    public FlowValue replace(@Nullable FlowValue withValue, Set<QualifierHierarchy> forHierarchies) {
        if (this.fastEquals(withValue)) {
            return this;
        }

        QualifiedType<ProductSlot> resultType = this.type;
        if (withValue != null) {
            // replace slots in this with the slots in withValue for the given hierarchies
            TypeMirror thisJavaType = type.getJavaType();
            TypeMirror otherJavaType = withValue.type.getJavaType();
            if (!types.isSameType(thisJavaType, otherJavaType)) {
                throw new PluginError("Replacing %s with %s is not supported", thisJavaType, otherJavaType);
            }

            resultType = ProductSlotUtils.replace(context, this.type, withValue.type, forHierarchies);
        } else if (ProductSlotUtils.hasHierarchies(this.type, forHierarchies)) {
            // remove slots in this.type that are associated with the given hierarchy
            resultType = ProductSlotUtils.remove(context, this.type, forHierarchies);
        }

        if (resultType == this.type) {
            return this;
        }
        return new FlowValue(context, resultType);
    }

    // returns a non-null FlowValue if one of the arguments is non-null
    public static @PolyNull FlowValue replace(@PolyNull FlowValue oldValue,
                                              @PolyNull FlowValue newValue,
                                              Set<QualifierHierarchy> forHierarchies) {
        if (oldValue != null) {
            return oldValue.replace(newValue, forHierarchies);
        }
        return newValue;
    }
}
