package org.cfginference.core.model.qualifier;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;

public interface Qualifier {
    AnnotationMirror toAnnotation(Elements elements);
}
