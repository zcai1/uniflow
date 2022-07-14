package org.cfginference.util;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.checkerframework.javacutil.Pair;
import scenelib.annotations.io.ASTIndex;
import scenelib.annotations.io.ASTPath;
import scenelib.annotations.io.ASTRecord;

import java.util.IdentityHashMap;
import java.util.logging.Logger;

/**
 * ASTPathUtil is a collection of utilities to create ASTRecord for existing trees, as well
 * as trees that are implied to exist but are not required by the compiler (e.g. extends Object).
 */
public class ASTPathUtil {

    protected static final Logger logger = Logger.getLogger(ASTPathUtil.class.getName());


    public static final String AFU_CONSTRUCTOR_ID = "<init>()V";


    /**
     * Look up an ASTRecord for a node.
     * @return The ASTRecord for node
     */
    public static ASTRecord getASTRecordForPath(TreePath pathToNode) {
        if (pathToNode == null) {
            return null;
        }

        Tree node = pathToNode.getLeaf();

        // ASTIndex caches the lookups, so we don't.
        if (ASTIndex.indexOf(pathToNode.getCompilationUnit()).containsKey(node)) {
            ASTRecord record = ASTIndex.indexOf(pathToNode.getCompilationUnit()).get(node);
            if (record == null) {
                logger.warning("ASTIndex returned null for record: " + node);
            }
            return record;
        } else {
            logger.fine("Did not find ASTRecord for node: " + node);
            return null;
        }
    }

    /**
     * Given the record for a class, return a new record that maps to the no-arg constructor for
     * this class.
     */
    public static ASTRecord getConstructorRecord(ASTRecord classRecord) {
        return new ASTRecord(classRecord.ast, classRecord.className, AFU_CONSTRUCTOR_ID, null, ASTPath.empty());
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
}
