package org.cfginference.core.model.util.formatter;

import org.cfginference.core.model.qualifier.Qualifier;
import org.cfginference.core.model.type.PrimaryQualifiedType;
import org.cfginference.core.model.type.QualifiedArrayType;
import org.cfginference.core.model.type.QualifiedDeclaredType;
import org.cfginference.core.model.type.QualifiedExecutableType;
import org.cfginference.core.model.type.QualifiedIntersectionType;
import org.cfginference.core.model.type.QualifiedNoType;
import org.cfginference.core.model.type.QualifiedNullType;
import org.cfginference.core.model.type.QualifiedPrimitiveType;
import org.cfginference.core.model.type.QualifiedType;
import org.cfginference.core.model.type.QualifiedTypeVariable;
import org.cfginference.core.model.type.QualifiedUnionType;
import org.cfginference.core.model.type.QualifiedWildcardType;
import org.cfginference.core.model.util.SimpleQualifiedTypeVisitor;
import org.checkerframework.javacutil.TypeAnnotationUtils;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class DefaultQualifiedTypeFormatter 
        extends SimpleQualifiedTypeVisitor<Qualifier, String, Void>
        implements QualifiedTypeFormatter {
    
    private DefaultQualifiedTypeFormatter() {}
    
    private static final DefaultQualifiedTypeFormatter instance = new DefaultQualifiedTypeFormatter();
    
    public static DefaultQualifiedTypeFormatter getInstance() {
        return instance;
    }
    
    @Override
    public String format(QualifiedType<?> type) {
        return this.visit((QualifiedType<Qualifier>) type, null);
    }

    @Override
    public String visitArray(QualifiedArrayType<Qualifier> type, Void p) {
        StringBuilder sb = new StringBuilder();

        QualifiedType<Qualifier> currentType = type;
        while (currentType instanceof QualifiedArrayType<Qualifier> arrayType) {
            sb.append(" ");
            sb.append(arrayType.getQualifier());
            sb.append("[]");
            currentType = arrayType.getComponentType();
        }

        // component type
        sb.insert(0, visit(currentType, p));
        return sb.toString();
    }

    @Override
    public String visitDeclared(QualifiedDeclaredType<Qualifier> type, Void p) {
        StringBuilder sb = new StringBuilder();
        
        if (type.getJavaType().getEnclosingType().getKind() != TypeKind.NONE) {
            sb.append(visit(type.getEnclosingType(), p));
        }

        Element typeElement = type.getJavaType().asElement();
        String typeName = typeElement.getSimpleName().toString();
        if (typeName.isEmpty()) {
            typeName = typeElement.toString();
        }

        sb.append(type.getQualifier());
        sb.append(" ");
        sb.append(typeName);

        // TODO(generics): type args
        return sb.toString();
    }

    @Override
    public String visitExecutable(QualifiedExecutableType<Qualifier> type, Void p) {
        StringBuilder sb = new StringBuilder();

        sb.append(visit(type.getReturnType(), p));
        sb.append(" ");

        if (type.getJavaElement() != null) {
            sb.append(type.getJavaElement().getSimpleName());
        } else {
            sb.append("METHOD");
        }

        sb.append("(");

        boolean hasReceiver = type.getJavaType().getReceiverType() != null
                && type.getJavaType().getReceiverType().getKind() != TypeKind.NONE;
        if (hasReceiver) {
            sb.append(visit(type.getReceiverType(), p));
            sb.append(" this");
        }

        int paramCount = 0;
        for (QualifiedType<Qualifier> param : type.getParameterTypes()) {
            if (hasReceiver || paramCount > 0) {
                sb.append(", ");
            }
            sb.append(visit(param, p));
            sb.append(" p");
            sb.append(paramCount++);
        }

        sb.append(")");

        boolean firstThrow = true;
        for (QualifiedType<Qualifier> thrownType: type.getThrownTypes()) {
            if (firstThrow) {
                sb.append(" throws ");
                firstThrow = false;
            } else {
                sb.append(", ");
            }
            sb.append(visit(thrownType, p));
        }
        return sb.toString();
    }

    @Override
    public String visitIntersection(QualifiedIntersectionType<Qualifier> type, Void p) {
        StringBuilder sb = new StringBuilder();

        sb.append(type.getQualifier());

        sb.append("(");
        boolean first = true;
        for (QualifiedType<Qualifier> bound : type.getBounds()) {
            if (!first) {
                sb.append(" & ");
            }
            sb.append(visit(bound, p));
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String visitNo(QualifiedNoType<Qualifier> type, Void p) {
        return formatFlatType(type);
    }

    @Override
    public String visitNull(QualifiedNullType<Qualifier> type, Void p) {
        return formatFlatType(type);
    }

    @Override
    public String visitPrimitive(QualifiedPrimitiveType<Qualifier> type, Void p) {
        return formatFlatType(type);
    }

    @Override
    public String visitUnion(QualifiedUnionType<Qualifier> type, Void p) {
        StringBuilder sb = new StringBuilder();

        sb.append(type.getQualifier());

        sb.append("(");
        boolean first = true;
        for (QualifiedType<Qualifier> alt : type.getAlternatives()) {
            if (!first) {
                sb.append(" | ");
            }
            sb.append(visit(alt, p));
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String visitTypeVariable(QualifiedTypeVariable<Qualifier> type, Void p) {
        // TODO(generics): implementation
        throw new UnsupportedOperationException();
    }

    @Override
    public String visitWildcard(QualifiedWildcardType<Qualifier> type, Void p) {
        // TODO(generics): implementation
        throw new UnsupportedOperationException();
    }

    protected String formatFlatType(QualifiedType<?> flatType) {
        TypeMirror unannotatedType = TypeAnnotationUtils.unannotatedType(flatType.getJavaType());
        if (flatType instanceof PrimaryQualifiedType<?> pqFlatType) {
            return pqFlatType.getQualifier().toString() + " " + unannotatedType;
        }
        return unannotatedType.toString();
    }
}
