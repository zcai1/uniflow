package org.uniflow.core.model.location;

// Where is this TypeMirror used?
// See JLS 4.11
public enum TypeContext {

    // look at scala
    // defaults for field reads and writes
    // for byte code elements, reading a field and writing to a field can be different
    CLASS_EXTENDS_OR_IMPLEMENTS,
    METHOD_RETURN,
    THROWS_CLAUSE,
    TYPE_PARAM_EXTENDS,
    FIELD,
    PARAMETER,
    METHOD_RECEIVER,
    LOCAL_VARIABLE,
    EXCEPTION_PARAM,
    RECORD_COMPONENT,

    TYPE_ARGUMENT,
    NEW_ARRAY,
    NEW_CLASS,
    CAST,
    INSTANCE_OF,
    METHOD_REFERENCE,
    EXPRESSION;

    enum NestingKind {
        ARRAY_COMPONENT,
        ENCLOSING_TYPE,
        UPPER_BOUND,
        LOWER_BOUND,
        TYPE_ARGUMENT,
        RETURN_TYPE,
        PARAMETER_TYPE,
        RECEIVER_TYPE,
        THROWN_TYPE,
        UNION_COMPONENT,
        INTERSECTION_COMPONENT,
    }
}
