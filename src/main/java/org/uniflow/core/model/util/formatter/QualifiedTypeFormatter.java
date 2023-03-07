package org.uniflow.core.model.util.formatter;

import org.uniflow.core.model.type.QualifiedType;

public interface QualifiedTypeFormatter {

    public String format(QualifiedType<?> type);
}
