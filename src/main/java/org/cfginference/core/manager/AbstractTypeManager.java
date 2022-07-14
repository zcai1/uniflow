package org.cfginference.core.manager;

import com.sun.source.tree.Tree;
import org.cfginference.core.model.element.QualifiedElement;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.type.QualifiedType;

import javax.lang.model.element.Element;

public abstract class AbstractTypeManager<Q extends Qualifier> implements TypeManager<Q> {
    @Override
    public QualifiedType<Q> getQualifiedType(Tree tree) {
        return null;
    }

    @Override
    public QualifiedElement<Q> getQualifiedElement(Element element) {

        return null;
    }
}
