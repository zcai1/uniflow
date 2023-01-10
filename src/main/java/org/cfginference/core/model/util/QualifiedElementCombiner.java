package org.cfginference.core.model.util;

import com.google.common.base.Preconditions;
import org.cfginference.core.model.element.PrimaryQualifiedElement;
import org.cfginference.core.model.element.QualifiedElement;
import org.cfginference.core.model.element.QualifiedExecutableElement;
import org.cfginference.core.model.element.QualifiedRecordComponentElement;
import org.cfginference.core.model.element.QualifiedTypeElement;
import org.cfginference.core.model.element.QualifiedTypeParameterElement;
import org.cfginference.core.model.element.QualifiedVariableElement;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.type.QualifiedType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class QualifiedElementCombiner<Q1 extends Qualifier, Q2 extends Qualifier, RQ extends Qualifier>
        extends DoubleQualifiedElementVisitor<Q1, Q2, QualifiedElement<RQ>> {

    protected abstract RQ getQualifier(PrimaryQualifiedElement<Q1> element1, PrimaryQualifiedElement<Q2> element2);

    protected abstract QualifiedType<RQ> getQualifiedType(QualifiedType<Q1> type1, QualifiedType<Q2> type2);

    protected List<QualifiedType<RQ>> getQualifiedType(List<? extends QualifiedType<Q1>> types1,
                                                       List<? extends QualifiedType<Q2>> types2) {
        Preconditions.checkArgument(types1.size() == types2.size());

        List<QualifiedType<RQ>> rs = new ArrayList<>(types1.size());
        Iterator<? extends QualifiedType<Q1>> iter1 = types1.iterator();
        Iterator<? extends QualifiedType<Q2>> iter2 = types2.iterator();
        while (iter1.hasNext()) {
            rs.add(getQualifiedType(iter1.next(), iter2.next()));
        }
        return rs;
    }

    @Override
    @SuppressWarnings("unchecked")
    public QualifiedElement<RQ> visitExecutable(QualifiedExecutableElement<Q1> element1,
                                                QualifiedExecutableElement<Q2> element2) {
        assert element1.getJavaElement() == element2.getJavaElement();

        return QualifiedExecutableElement.<RQ>builder()
                .setJavaElement(element1.getJavaElement())
                .setParameters(((List) visit(element1.getParameters(), element2.getParameters())))
                .setTypeParameters(visit(element1.getTypeParameters(), element2.getTypeParameters()))
                .setReceiverType(getQualifiedType(element1.getReceiverType(), element2.getReceiverType()))
                .setReturnType(getQualifiedType(element1.getReturnType(), element2.getReturnType()))
                .setThrownTypes(getQualifiedType(element1.getThrownTypes(), element2.getThrownTypes()))
                .build();
    }

    @Override
    public QualifiedElement<RQ> visitRecordComponent(QualifiedRecordComponentElement<Q1> element1,
                                                     QualifiedRecordComponentElement<Q2> element2) {
        assert element1.getJavaElement() == element2.getJavaElement();

        return QualifiedRecordComponentElement.<RQ>builder()
                .setJavaElement(element1.getJavaElement())
                .setAccessor((QualifiedExecutableElement<RQ>) visit(element1.getAccessor(), element2.getAccessor()))
                .setType(getQualifiedType(element1.getType(), element2.getType()))
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public QualifiedElement<RQ> visitType(QualifiedTypeElement<Q1> element1, QualifiedTypeElement<Q2> element2) {
        assert element1.getJavaElement() == element2.getJavaElement();

        return QualifiedTypeElement.<RQ>builder()
                .setJavaElement(element1.getJavaElement())
                .setQualifier(getQualifier(element1, element2))
                .setInterfaces(getQualifiedType(element1.getInterfaces(), element2.getInterfaces()))
                .setSuperClass(getQualifiedType(element1.getSuperClass(), element2.getSuperClass()))
                .setTypeParameters((List) visit(element1.getTypeParameters(), element2.getTypeParameters()))
                .setRecordComponents(visit(element1.getRecordComponents(), element2.getRecordComponents()))
                .build();
    }

    @Override
    public QualifiedElement<RQ> visitTypeParameter(QualifiedTypeParameterElement<Q1> element1,
                                                   QualifiedTypeParameterElement<Q2> element2) {
        // TODO(generics): implementation
        throw new UnsupportedOperationException();
    }

    @Override
    public QualifiedElement<RQ> visitVariable(QualifiedVariableElement<Q1> element1,
                                              QualifiedVariableElement<Q2> element2) {
        assert element1.getJavaElement() == element2.getJavaElement();

        return QualifiedVariableElement.<RQ>builder()
                .setJavaElement(element1.getJavaElement())
                .setType(getQualifiedType(element1.getType(), element2.getType()))
                .build();
    }
}
