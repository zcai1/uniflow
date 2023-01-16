package org.tainting;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.typesystem.AbstractQualifierHierarchy;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.tainting.qual.Tainted;
import org.checkerframework.checker.tainting.qual.Untainted;
import org.checkerframework.javacutil.AnnotationUtils;

import javax.lang.model.element.AnnotationMirror;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class TaintingQualifierHierarchy extends AbstractQualifierHierarchy {

    private Set<Qualifier> qualifiers = Collections.unmodifiableSet(EnumSet.allOf(TaintQual.class));

    @Override
    public boolean isInHierarchy(Qualifier qualifier) {
        return qualifier instanceof TaintQual;
    }

    @Override
    public Qualifier getTopQualifier() {
        return TaintQual.TAINTED;
    }

    @Override
    public Qualifier getBottomQualifier() {
        return TaintQual.UNTAINTED;
    }

    @Override
    public @Nullable Qualifier getPolymorphicQualifier() {
        return null;
    }

    @Override
    public boolean isPolymorphicQualifier(Qualifier qualifier) {
        return false;
    }

    @Override
    public boolean isSubtype(Qualifier subQualifier, Qualifier superQualifier) {
        if (subQualifier instanceof TaintQual subQual && superQualifier instanceof TaintQual superQual) {
            return subQual.ordinal() <= superQual.ordinal();
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Qualifier leastUpperBound(Qualifier qualifier1, Qualifier qualifier2) {
        if (qualifier1 instanceof TaintQual q1 && qualifier2 instanceof TaintQual q2) {
            if (q1 == TaintQual.TAINTED || q2 == TaintQual.TAINTED) {
                return TaintQual.TAINTED;
            }
            return TaintQual.UNTAINTED;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Qualifier greatestLowerBound(Qualifier qualifier1, Qualifier qualifier2) {
        if (qualifier1 instanceof TaintQual q1 && qualifier2 instanceof TaintQual q2) {
            if (q1 == TaintQual.UNTAINTED || q2 == TaintQual.UNTAINTED) {
                return TaintQual.UNTAINTED;
            }
            return TaintQual.TAINTED;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public @Nullable Qualifier getQualifier(AnnotationMirror annotationMirror) {
        Class<?> annoClass = AnnotationUtils.annotationMirrorToClass(annotationMirror);
        if (annoClass == Tainted.class) {
            return TaintQual.TAINTED;
        } else if (annoClass == Untainted.class) {
            return TaintQual.UNTAINTED;
        }
        return null;
    }

    @Override
    public Set<Qualifier> getAllDefaultQualifiers() {
        return qualifiers;
    }
}
