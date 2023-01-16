package org.cfginference.core.model.reporting;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import org.checkerframework.checker.compilermsgs.qual.CompilerMessageKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.javacutil.ElementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scenelib.annotations.io.ASTIndex;

import javax.lang.model.element.Element;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.Properties;

@AutoValue
public abstract class AnalysisMessage {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisMessage.class);

    // Element or JavaFileObject(source file)
    public abstract Object getSource();

    // getSource() is JavaFileObject <==> getPosition() != null
    public abstract @Nullable DiagnosticPosition getPosition();

    public abstract Diagnostic.Kind getKind();

    public abstract @CompilerMessageKey String getMessageKey();

    // TODO: How to handle ProductSlots? We need to know the exact qualifiers.
    // TODO: How to handle variable slots? Will they make sense?
    public abstract ImmutableList<Object> getArgs();

    public static AnalysisMessage create(Object source,
                                         @Nullable DiagnosticPosition position,
                                         Diagnostic.Kind kind,
                                         @CompilerMessageKey String messageKey,
                                         Object... args) {
        Preconditions.checkArgument(source instanceof Element
                || (source instanceof JavaFileObject && position != null));
        return new AutoValue_AnalysisMessage(source, position, kind, messageKey, ImmutableList.copyOf(args));
    }

    public static AnalysisMessage create(CompilationUnitTree currentRoot,
                                         Object source,
                                         Diagnostic.Kind kind,
                                         @CompilerMessageKey String messageKey,
                                         Object... args) {
        DiagnosticPosition position = null;

        // TODO: it's strange to allow tree as source, probably move this to a utility method
        if (source instanceof Tree tree) {
            if (!ASTIndex.indexOf(currentRoot).containsKey(tree)) {
                // try to verify the tree is under the current root
                logger.warn("Tree {} is probably not under the current root {}",
                        tree, currentRoot.getSourceFile().toUri());
            }

            // making the assumption that the tree is under the current root
            source = currentRoot.getSourceFile();
            position = SimpleDiagnosticPosition.create(currentRoot, tree);
        }
        return create(source, position, kind, messageKey, args);
    }

    public static AnalysisMessage createError(CompilationUnitTree currentRoot,
                                              Object source,
                                              @CompilerMessageKey String messageKey,
                                              Object... args) {
        return create(currentRoot, source, Diagnostic.Kind.ERROR, messageKey, args);
    }

    public static AnalysisMessage createError(Object source,
                                              @Nullable DiagnosticPosition position,
                                              @CompilerMessageKey String messageKey,
                                              Object... args) {
        return create(source, position, Diagnostic.Kind.ERROR, messageKey, args);
    }

    public static AnalysisMessage createWarning(CompilationUnitTree currentRoot,
                                                Object source,
                                                @CompilerMessageKey String messageKey,
                                                Object... args) {
        return create(currentRoot, source, Diagnostic.Kind.WARNING, messageKey, args);
    }

    public static AnalysisMessage createWarning(Object source,
                                                @Nullable DiagnosticPosition position,
                                                @CompilerMessageKey String messageKey,
                                                Object... args) {
        return create(source, position, Diagnostic.Kind.WARNING, messageKey, args);
    }

    public String getFormattedMessage(Properties properties) {
        String rawMessage = (String) properties.get(getMessageKey());
        String msg;
        try {
            msg = rawMessage.formatted(getArgs().toArray());
        } catch (MissingFormatArgumentException e) {
            throw new PluginError("Missing format argument for " + getMessageKey(), e);
        }

        // try to provide some location information
        Object src = getSource();
        if (src instanceof Element currentElement) {
            List<Element> pathToTop = new ArrayList<>();
            while (true) {
                pathToTop.add(currentElement);

                if (ElementUtils.isTypeElement(currentElement)
                        && ((TypeElement) currentElement).getNestingKind() == NestingKind.TOP_LEVEL) {
                    break;
                }
                currentElement = currentElement.getEnclosingElement();
            }

            StringBuilder sb = new StringBuilder();
            sb.append(msg);
            sb.append("\n");
            sb.append("Source is an unlocated element: ");

            int indexOfTop = pathToTop.size() - 1;
            for (int i = indexOfTop; i >= 0; --i) {
                Element e = pathToTop.get(i);
                if (i != indexOfTop) {
                    sb.append("#");
                }
                sb.append(e);
            }
            msg = sb.toString();
        }

        return msg;
    }
}
