package org.cfginference.core.model.util.formatter;

import org.cfginference.core.model.type.QualifiedType;

public interface QualifiedTypeFormatter {

    public String format(QualifiedType<?> type);
}
