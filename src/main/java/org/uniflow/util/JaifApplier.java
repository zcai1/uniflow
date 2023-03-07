package org.uniflow.util;

import com.sun.tools.javac.main.Arguments;
import com.sun.tools.javac.util.Context;
import org.uniflow.core.PluginOptions;
import org.uniflow.core.model.reporting.PluginError;

import javax.tools.JavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JaifApplier {

    private JaifApplier() {}

    public static void apply(Context context) {
        PluginOptions options = PluginOptions.instance(context);
        List<String> inputFiles = getInputJavaFiles(context);
        String insertScriptPath = getInsertScriptPath(options);
        String jaifPath = Objects.requireNonNull(options.getJaifOutputPath());
        String outputPath = Objects.requireNonNull(options.getAfuOutputDir());

        File outputDir = new File(outputPath);
        ensureDirectoryExists(outputDir);

        List<String> command = new ArrayList<>();
        command.add(insertScriptPath);
        command.add("-v");
        command.add("--print-error-stack=true");
        command.add("--outdir=" + outputDir.getAbsolutePath());
        command.add(jaifPath);
        command.addAll(inputFiles);

        ByteArrayOutputStream insertOut = new ByteArrayOutputStream();
        int exitCode;
        try {
            Process p = new ProcessBuilder(command)
                    .inheritIO()
                    .start();
            exitCode = p.waitFor();

        } catch (IOException e) {
            throw new PluginError(e);
        } catch (InterruptedException e) {
            throw new PluginError(e);
        }

        if (exitCode != 0) {
            throw new PluginError("Failed to apply jaif");
        }
    }

    private static String getInsertScriptPath(PluginOptions options) {
        String pathToAfuScripts = options.getPathToAfuScripts();
        String insertScriptPath = "insert-annotations-to-source";
        if (pathToAfuScripts != null) {
            insertScriptPath = pathToAfuScripts + File.separator + insertScriptPath;
        }
        return insertScriptPath;
    }

    private static void ensureDirectoryExists(File path) {
        if (!path.exists()) {
            if (!path.mkdirs()) {
                throw new PluginError("Could not make directory: " + path.getAbsolutePath());
            }
        }
    }

    private static List<String> getInputJavaFiles(Context context) {
        List<String> files = new ArrayList<>();
        // TODO: find a better (more stable) way
        Arguments arguments = Arguments.instance(context);

        for (JavaFileObject fileObject : arguments.getFileObjects()) {
            if (fileObject.getKind() == JavaFileObject.Kind.SOURCE) {
                URI fileUri = fileObject.toUri();
                if (fileUri.getScheme().equalsIgnoreCase("file")) {
                    files.add(fileUri.getPath());
                }
            }
        }
        return files;
    }
}
