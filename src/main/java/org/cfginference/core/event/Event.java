package org.cfginference.core.event;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;

public interface Event {
    record NewAnalysisTask(CompilationUnitTree root, ClassTree topLevelClass) implements Event {}
}
