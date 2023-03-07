package org.uniflow.util;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.uniflow.core.model.qualifier.Qualifier;
import org.uniflow.core.model.type.PrimaryQualifiedType;
import org.uniflow.core.model.type.QualifiedArrayType;
import org.uniflow.core.model.type.QualifiedDeclaredType;
import org.uniflow.core.model.type.QualifiedExecutableType;
import org.uniflow.core.model.type.QualifiedIntersectionType;
import org.uniflow.core.model.type.QualifiedNullType;
import org.uniflow.core.model.type.QualifiedPrimitiveType;
import org.uniflow.core.model.type.QualifiedType;
import org.uniflow.core.model.type.QualifiedTypeVariable;
import org.uniflow.core.model.type.QualifiedUnionType;
import org.uniflow.core.model.type.QualifiedWildcardType;
import org.uniflow.core.model.util.SimpleQualifiedTypeVisitor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.javacutil.Pair;
import scenelib.annotations.io.ASTIndex;
import scenelib.annotations.io.ASTPath;
import scenelib.annotations.io.ASTRecord;

import javax.lang.model.type.TypeKind;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ASTPathUtil is a collection of utilities to create ASTRecord for existing trees, as well
 * as trees that are implied to exist but are not required by the compiler (e.g. extends Object).
 */
public class ASTPathUtils {

    protected static final Logger logger = Logger.getLogger(ASTPathUtils.class.getName());


    public static final String AFU_CONSTRUCTOR_ID = "<init>()V";

    private ASTPathUtils() {}

    /**
     * Look up an ASTRecord for a node.
     * @return The ASTRecord for node
     */
    public static @Nullable ASTRecord getASTRecord(TreePath pathToNode) {
        return getASTRecord(pathToNode.getCompilationUnit(), pathToNode.getLeaf());
    }

    public static @Nullable ASTRecord getASTRecord(CompilationUnitTree root, Tree tree) {
        // ASTIndex caches the lookups, so we don't.
        Map<Tree, ASTRecord> astIndex = ASTIndex.indexOf(root);
        if (astIndex.containsKey(tree)) {
            ASTRecord record = astIndex.get(tree);
            if (record == null) {
                logger.warning("ASTIndex returned null for tree: " + tree);
            }
            return rootErased(record);
        } else {
            logger.fine("Did not find ASTRecord for tree: " + tree);
            return null;
        }
    }

    /**
     * Given the record for a class, return a new record that maps to the no-arg constructor for
     * this class.
     */
    public static ASTRecord getConstructorRecord(ASTRecord classRecord) {
        return new ASTRecord(null, classRecord.className, AFU_CONSTRUCTOR_ID, null, ASTPath.empty());
    }

    /**
     * Erase ASTRecord.ast to prevent memory leak.
     */
    public static @PolyNull ASTRecord rootErased(@PolyNull ASTRecord record) {
        if (record == null) {
            return null;
        }

        if (record.ast == null) {
            return record;
        }
        return new ASTRecord(null, record.className, record.methodName, record.varName, record.astPath);
    }

    /**
     * Converts fully qualified class name into a pair of Strings (packageName -> className)
     */
    public static Pair<String, String> splitFullyQualifiedClass(String fullClassname) {
        String pkgName;
        String className;
        int lastPeriod = fullClassname.lastIndexOf(".");
        if (lastPeriod == -1) {
            // default package
            pkgName = "";
            className = fullClassname;
        } else {
            pkgName = fullClassname.substring(0, lastPeriod);
            className = fullClassname.substring(lastPeriod + 1, fullClassname.length());
        }

        return Pair.of(pkgName, className);
    }

    /**
     * Sometimes there are trees that are implied by the source code but not explicitly written.  (e.g. extends Object)
     * The AFU will create these trees if we try to insert an annotation on them.  Therefore, we need to create
     * an AFU record that corresponds to the location on the "implied" (non-existant) tree.
     *
     * This method creates a mapping of AnnotatedTypeMirrors that are children of type and an ASTRecord that
     * corresponds to annotating the primary annotation of that type.
     */
    public static <Q extends Qualifier> IdentityHashMap<PrimaryQualifiedType<Q>, ASTRecord> getImpliedRecordForUse(
            ASTRecord astRecord,
            QualifiedType<Q> type
    ) {
        AFUPathMapper<Q> mapper = new AFUPathMapper<>();
        mapper.visit(type, astRecord);
        return mapper.mapping;
    }

    protected static class AFUPathMapper<Q extends Qualifier> extends SimpleQualifiedTypeVisitor<Q, Void, ASTRecord> {

        final IdentityHashMap<PrimaryQualifiedType<Q>, ASTRecord> mapping = new IdentityHashMap<>();

        @Override
        public Void visitArray(QualifiedArrayType<Q> type, ASTRecord astRecord) {
            // TODO: THERE DOESN'T SEEM TO BE A WAY TO REFERENCE THE COMPONENT TYPE
            throw new UnsupportedOperationException();
        }

        @Override
        public Void visitDeclared(QualifiedDeclaredType<Q> type, ASTRecord astRecord) {
            if (!save(type, astRecord)) {
                return null;
            }

            int typeArgIndex = 0;
            for (QualifiedType<Q> typeArg : type.getTypeArguments()) {
                ASTRecord next = astRecord.extend(Tree.Kind.PARAMETERIZED_TYPE, ASTPath.TYPE_ARGUMENT, typeArgIndex);
                visit(typeArg, next);
                ++typeArgIndex;
            }

            QualifiedType<Q> enclosingType = type.getEnclosingType();
            if (enclosingType.getJavaType().getKind() != TypeKind.NONE) {
                ASTRecord next = astRecord.extend(Tree.Kind.MEMBER_SELECT, ASTPath.EXPRESSION);
                visit(enclosingType, next);
            }
            return null;
        }

        @Override
        public Void visitExecutable(QualifiedExecutableType<Q> type, ASTRecord astRecord) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Void visitIntersection(QualifiedIntersectionType<Q> type, ASTRecord astRecord) {
            int boundIndex = 0;
            for (QualifiedType<Q> bound : type.getBounds()) {
                ASTRecord toBound = astRecord.extend(Tree.Kind.INTERSECTION_TYPE, ASTPath.BOUND, boundIndex);
                visit(bound, toBound);
                ++boundIndex;
            }
            return null;
        }

        @Override
        public Void visitUnion(QualifiedUnionType<Q> type, ASTRecord astRecord) {
            int alternativeIndex = 0;
            for (QualifiedType<Q> alt : type.getAlternatives()) {
                ASTRecord toAlt = astRecord.extend(Tree.Kind.UNION_TYPE, ASTPath.TYPE_ALTERNATIVE, alternativeIndex);
                visit(alt, toAlt);
                ++alternativeIndex;
            }
            return null;
        }

        @Override
        public Void visitNull(QualifiedNullType<Q> type, ASTRecord astRecord) {
            save(type, astRecord);
            return null;
        }

        @Override
        public Void visitPrimitive(QualifiedPrimitiveType<Q> type, ASTRecord astRecord) {
            save(type, astRecord);
            return null;
        }

        @Override
        public Void visitTypeVariable(QualifiedTypeVariable<Q> type, ASTRecord astRecord) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        @Override
        public Void visitWildcard(QualifiedWildcardType<Q> type, ASTRecord astRecord) {
            // TODO(generics): implementation
            throw new UnsupportedOperationException();
        }

        private boolean save(PrimaryQualifiedType<Q> type, ASTRecord astRecord) {
            if (mapping.containsKey(type)) {
                return false;
            }
            mapping.put(type, astRecord);
            return true;
        }
    }
}
