package org.cfginference.core.model.qualifier;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableMap;

import javax.lang.model.element.AnnotationValue;
import java.lang.annotation.Annotation;
import java.util.Map;

@AutoValue
public abstract class AnnotationProxy {

    public abstract Class<? extends Annotation> getAnnotationClass();

    public abstract ImmutableMap<String, AnnotationValue> getValues();

    public static AnnotationProxy create(Class<? extends Annotation> annotationClass,
                                         Map<String, AnnotationValue> values) {
        return new AutoValue_AnnotationProxy(annotationClass, ImmutableMap.copyOf(values));
    }

    public static AnnotationProxy create(Class<? extends Annotation> annotationClass) {
        return new AutoValue_AnnotationProxy(annotationClass, ImmutableMap.of());
    }

    @Override
    @Memoized
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("@");
        buf.append(getAnnotationClass().getCanonicalName().intern());
        int len = getValues().size();
        if (len > 0) {
            buf.append('(');
            boolean first = true;
            for (Map.Entry<String, AnnotationValue> entry : getValues().entrySet()) {
                if (!first) {
                    buf.append(", ");
                }
                first = false;

                String name = entry.getKey();
                if (len > 1 || !name.equals("value")) {
                    buf.append(name);
                    buf.append('=');
                }
                buf.append(entry.getValue());
            }
            buf.append(')');
        }
        return buf.toString().intern();
    }
}
