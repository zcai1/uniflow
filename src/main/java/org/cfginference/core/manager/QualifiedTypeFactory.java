package org.cfginference.core.manager;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.type.QualifiedType;

import javax.lang.model.type.TypeMirror;

public class QualifiedTypeFactory {
    public static <Q extends Qualifier> QualifiedType<Q> createRaw(TypeMirror type) {
        return null;
    }
}
