package org.cfginference.core.event;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;

public interface Event {
    enum SimpleEvent implements Event {
        FULL_ANALYSIS
    }

    record NewAnalysisTask(CompilationUnitTree root, ClassTree topLevelClass) implements Event {}
}
