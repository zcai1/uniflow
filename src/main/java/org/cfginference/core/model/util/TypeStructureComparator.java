package org.cfginference.core.model.util;

import com.google.common.base.Preconditions;
import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.type.QualifiedType;

import java.util.Collection;

public final class TypeStructureComparator extends DoubleQualifiedTypeScanner<Qualifier, Qualifier, Boolean> {

    public boolean areEqual(QualifiedType type1, QualifiedType type2) {
        return scan(type1, type2);
    }

    @Override
    protected Boolean scan(Collection<? extends QualifiedType<Qualifier>> types1,
                           Collection<? extends QualifiedType<Qualifier>> types2) {
        if (types1.size() != types2.size()) {
            return false;
        }
        return super.scan(types1, types2);
    }

    @Override
    public Boolean scan(QualifiedType<Qualifier> type1, QualifiedType<Qualifier> type2) {
        Preconditions.checkNotNull(type1);
        Preconditions.checkNotNull(type2);

        if (type1 == type2) {
            return true;
        }
        return type1.getKind() == type2.getKind();
    }

    @Override
    protected Boolean scanAndReduce(Collection<? extends QualifiedType<Qualifier>> types1,
                                    Collection<? extends QualifiedType<Qualifier>> types2,
                                    Boolean r) {
        return r && scan(types1, types2);
    }

    @Override
    protected Boolean scanAndReduce(QualifiedType<Qualifier> type1,
                                    QualifiedType<Qualifier> type2,
                                    Boolean r) {
        return r && scan(type1, type2);
    }
}
