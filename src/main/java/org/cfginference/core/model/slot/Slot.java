package org.cfginference.core.model.slot;

import org.cfginference.core.annotation.VarAnnot;
import org.cfginference.core.model.location.QualifierLocation;
import org.cfginference.core.model.qualifier.Qualifier;
import org.checkerframework.javacutil.AnnotationBuilder;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.util.Elements;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Slots represent constraint variables.
 *
 * Each Slot has a unique identification number.
 *
 * Slots are represented by {@code @VarAnnot( slot id )} annotations in AnnotatedTypeMirrors.
 * The {@link checkers.inference.VariableAnnotator} generates the Slots for source code.
 *
 * A slot maintains the set of {@link MergeSlot}s of least-upper bound computations it is
 * involved in.
 *
 */
public abstract class Slot implements Qualifier, Comparable<Slot> {

    private static final AtomicInteger instanceCounter = new AtomicInteger();

    /**
     * Uniquely identifies this Slot.  id's are monotonically increasing in value by the order they
     * are generated
     */
    protected final int id;

    /**
     * Used to locate this Slot in source code. {@code AnnotationLocation}s are written to Jaif files
     * along with the annotations determined for this slot by the Solver.
     */
    protected final QualifierLocation location;

    /**
     * Slots this variable has been merged to.
     */
    private final Set<MergeSlot> mergedToSlots = new HashSet<>();

    public Slot(QualifierLocation location) {
        this.id = instanceCounter.incrementAndGet();
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public QualifierLocation getLocation() {
        return location;
    }

    public abstract boolean isInsertable();

    public Set<MergeSlot> getMergedToSlots() {
        return Collections.unmodifiableSet(mergedToSlots);
    }

    public void addMergedToSlot(MergeSlot mergeSlot) {
        this.mergedToSlots.add(mergeSlot);
    }

    public boolean isMergedTo(Slot other) {
        for (MergeSlot mergedTo: mergedToSlots) {
            if (mergedTo.equals(other)) {
                return true;
            } else {
                if (mergedTo.isMergedTo(other)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public final AnnotationMirror toAnnotation(Elements elements) {
        return AnnotationBuilder.fromClass(
                elements,
                VarAnnot.class,
                AnnotationBuilder.elementNamesValues("value", id)
        );
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Slot other = (Slot) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public int compareTo(Slot other) {
        return Integer.compare(id, other.id);
    }

    public abstract Kind getKind();

    public enum Kind {
        SOURCE_VARIABLE,
        REFINEMENT,
        EXISTENTIAL,
        VIEWPOINT_ADAPTION,
        ARITHMETIC,
        COMPARISON,
        MERGE,
        POLYMORPHIC_INSTANCE,
        CONSTANT,
    }
}

