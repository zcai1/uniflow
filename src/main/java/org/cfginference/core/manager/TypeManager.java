package org.cfginference.core.manager;

import com.sun.source.tree.Tree;
import org.cfginference.core.model.element.QualifiedElement;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.type.QualifiedType;
import org.checkerframework.dataflow.cfg.node.Node;

import javax.lang.model.element.Element;

public interface TypeManager<Q extends Qualifier> {
    default QualifiedType<Q> getQualifiedType(Node node) {
        return getQualifiedType(node.getTree());
    }

    QualifiedType<Q> getQualifiedType(Tree tree);

    QualifiedElement<Q> getQualifiedElement(Element element);
}
