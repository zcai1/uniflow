package org.cfginference.core.solver.backend;

import com.sun.tools.javac.util.Context;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.util.serialization.Serializer;

/**
 * Translator is responsible for encoding/decoding work for Backend.
 * <p>
 * It encode Slot and Constraint to specific types needed by underlying solver,
 * and decode solver solution to AnnotationMirror.
 *
 * @author charleszhuochen
 *
 * @param <SlotEncodingT> encoding type for slot.
 * @param <ConstraintEncodingT> encoding type for constraint.
 * @param <SlotSolutionT> type for underlying solver's solution of a Slot
 */
public interface FormatTranslator<SlotEncodingT, ConstraintEncodingT, SlotSolutionT>
        extends Serializer<SlotEncodingT, ConstraintEncodingT> {

    /**
     * Decode solver's solution of a Slot to an AnnotationMirror represent this solution.
     *
     * @param solution solver's solution of a Slot
     * @param context the context for accessing utils, if needed
     * @return AnnotationMirror represent this solution
     */
    Qualifier decodeSolution(SlotSolutionT solution, Context context);
}
