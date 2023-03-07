package org.uniflow.core.model.util;

import org.uniflow.core.model.element.QualifiedElement;
import org.uniflow.core.model.element.QualifiedExecutableElement;
import org.uniflow.core.model.element.QualifiedRecordComponentElement;
import org.uniflow.core.model.element.QualifiedTypeElement;
import org.uniflow.core.model.element.QualifiedTypeParameterElement;
import org.uniflow.core.model.element.QualifiedVariableElement;
import org.uniflow.core.model.qualifier.Qualifier;

public class QualifiedElementScanner<Q extends Qualifier, R, P> implements QualifiedElementVisitor<Q, R, P>  {

    protected final R DEFAULT_VALUE;

    protected QualifiedElementScanner() {
        this.DEFAULT_VALUE = null;
    }

    protected QualifiedElementScanner(R defaultValue) {
        this.DEFAULT_VALUE = defaultValue;
    }

    protected R defaultAction(QualifiedElement<Q> type, P p) {
        return DEFAULT_VALUE;
    }

    protected R scan(QualifiedElement<Q> type, P p) {
        return (type == null) ? null : type.accept(this, p);
    }

    protected R scan(Iterable<? extends QualifiedElement<Q>> types, P p) {
        R r = DEFAULT_VALUE;
        if (types != null) {
            boolean first = true;
            for (QualifiedElement<Q> type : types) {
                r = (first ? scan(type, p) : scanAndReduce(type, p, r));
                first = false;
            }
        }
        return r;
    }

    private R scanAndReduce(QualifiedElement<Q> type, P p, R r) {
        return reduce(scan(type, p), r);
    }

    private R scanAndReduce(Iterable<? extends QualifiedElement<Q>> types, P p, R r) {
        return reduce(scan(types, p), r);
    }

    public R reduce(R r1, R r2) {
        return r1;
    }
    
    @Override
    public R visitExecutable(QualifiedExecutableElement<Q> element, P p) {
        R r = scan(element.getParameters(), p);
        r = scanAndReduce(element.getTypeParameters(), p, r);
        return r;
    }

    @Override
    public R visitRecordComponent(QualifiedRecordComponentElement<Q> element, P p) {
        return scan(element.getAccessor(), p);
    }

    @Override
    public R visitType(QualifiedTypeElement<Q> element, P p) {
        R r = scan(element.getRecordComponents(), p);
        r = scanAndReduce(element.getTypeParameters(), p, r);
        return r;
    }

    @Override
    public R visitTypeParameter(QualifiedTypeParameterElement<Q> element, P p) {
        return defaultAction(element, p);
    }

    @Override
    public R visitVariable(QualifiedVariableElement<Q> element, P p) {
        return defaultAction(element, p);
    }
}
