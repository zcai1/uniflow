package org.cfginference.core.flow;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.event.Event;
import org.cfginference.core.event.EventManager;

public final class EnterAnalysis {

    private final ASTAnalysisScanner astAnalysisScanner;

    private final CFGAnalysisScanner cfgAnalysisScanner;

    private final EventManager eventManager;

    private EnterAnalysis(Context context) {
        this.astAnalysisScanner = ASTAnalysisScanner.instance(context);
        this.cfgAnalysisScanner = CFGAnalysisScanner.instance(context);
        this.eventManager = EventManager.instance(context);
    }

    public static EnterAnalysis instance(Context context) {
        EnterAnalysis instance = context.get(EnterAnalysis.class);
        if (instance == null) {
            instance = new EnterAnalysis(context);
        }
        return instance;
    }

    public void enter(TreePath path) {
        Event newAnalysisEvent = new Event.NewAnalysisTask(path.getCompilationUnit(), (ClassTree) path.getLeaf());
        eventManager.broadcast(newAnalysisEvent, true);

        cfgAnalysisScanner.scan(path, null);
        astAnalysisScanner.scan(path, null);

        eventManager.broadcast(newAnalysisEvent, false);
    }
}
