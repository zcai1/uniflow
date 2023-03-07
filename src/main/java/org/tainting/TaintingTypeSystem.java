package org.tainting;

import com.sun.tools.javac.util.Context;
import org.uniflow.core.typesystem.AbstractTypeSystem;

public class TaintingTypeSystem extends AbstractTypeSystem {

    public TaintingTypeSystem(Context context) {
        super(context, new TaintingQualifierHierarchy());
    }
}
