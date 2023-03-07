package org.uniflow.core.model.util;

import com.google.common.base.Preconditions;
import org.uniflow.core.model.element.QualifiedElement;
import org.uniflow.core.model.element.QualifiedExecutableElement;
import org.uniflow.core.model.element.QualifiedRecordComponentElement;
import org.uniflow.core.model.element.QualifiedTypeElement;
import org.uniflow.core.model.element.QualifiedTypeParameterElement;
import org.uniflow.core.model.element.QualifiedVariableElement;
import org.uniflow.core.model.qualifier.Qualifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class DoubleQualifiedElementVisitor<Q1 extends Qualifier, Q2 extends Qualifier, R> 
        extends SimpleQualifiedElementVisitor<Q1, R, QualifiedElement<Q2>> {

    protected List<R> visit(List<? extends QualifiedElement<Q1>> elements1, List<? extends QualifiedElement<Q2>> elements2) {
        Preconditions.checkArgument(elements1.size() == elements2.size());

        List<R> rs = new ArrayList<>(elements1.size());
        Iterator<? extends QualifiedElement<Q1>> iter1 = elements1.iterator();
        Iterator<? extends QualifiedElement<Q2>> iter2 = elements2.iterator();
        while (iter1.hasNext()) {
            rs.add(visit(iter1.next(), iter2.next()));
        }
        return rs;
    }

    @Override
    public final R visitExecutable(QualifiedExecutableElement<Q1> element1, QualifiedElement<Q2> p) {
        if (p instanceof QualifiedExecutableElement<Q2> element2) {
            return visitExecutable(element1, element2);
        }
        throw new IllegalArgumentException();
    }

    public R visitExecutable(QualifiedExecutableElement<Q1> element1, QualifiedExecutableElement<Q2> element2) {
        return defaultAction(element1, element2);
    }

    @Override
    public final R visitRecordComponent(QualifiedRecordComponentElement<Q1> element1, QualifiedElement<Q2> p) {
        if (p instanceof QualifiedRecordComponentElement<Q2> element2) {
            return visitRecordComponent(element1, element2);
        }
        throw new IllegalArgumentException();
    }

    public R visitRecordComponent(QualifiedRecordComponentElement<Q1> element1, QualifiedRecordComponentElement<Q2> element2) {
        return defaultAction(element1, element2);
    }

    @Override
    public final R visitType(QualifiedTypeElement<Q1> element1, QualifiedElement<Q2> p) {
        if (p instanceof QualifiedTypeElement<Q2> element2) {
            return visitType(element1, element2);
        }
        throw new IllegalArgumentException();
    }

    public R visitType(QualifiedTypeElement<Q1> element1, QualifiedTypeElement<Q2> element2) {
        return defaultAction(element1, element2);
    }

    @Override
    public final R visitTypeParameter(QualifiedTypeParameterElement<Q1> element1, QualifiedElement<Q2> p) {
        if (p instanceof QualifiedTypeParameterElement<Q2> element2) {
            return visitTypeParameter(element1, element2);
        }
        throw new IllegalArgumentException();
    }

    public R visitTypeParameter(QualifiedTypeParameterElement<Q1> element1, QualifiedTypeParameterElement<Q2> element2) {
        return defaultAction(element1, element2);
    }

    @Override
    public final R visitVariable(QualifiedVariableElement<Q1> element1, QualifiedElement<Q2> p) {
        if (p instanceof QualifiedVariableElement<Q2> element2) {
            return visitVariable(element1, element2);
        }
        throw new IllegalArgumentException();
    }

    public R visitVariable(QualifiedVariableElement<Q1> element1, QualifiedVariableElement<Q2> element2) {
        return defaultAction(element1, element2);
    }
}
