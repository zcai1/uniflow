package org.cfginference.core.model.constraint;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import org.cfginference.core.model.slot.Slot;
import org.cfginference.core.model.slot.ViewpointAdaptationSlot;
import org.cfginference.core.model.util.serialization.Serializer;

@AutoValue
public abstract class ViewpointAdaptationConstraint extends Constraint {

    public final Slot getReceiverSlot() {
        return getResult().getReceiverSlot();
    }

    public final Slot getDeclarationSlot() {
        return getResult().getDeclarationSlot();
    }

    public abstract ViewpointAdaptationSlot getResult();

    @Override
    @Memoized
    public ImmutableList<Slot> getSlots() {
        return ImmutableList.of(getReceiverSlot(), getDeclarationSlot(), getResult());
    }

    @Override
    public final <S, T> T serialize(Serializer<S, T> serializer) {
        return serializer.serialize(this);
    }
}
