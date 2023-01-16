package org.cfginference.core.flow;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.util.Context;
import org.cfginference.core.event.Event;
import org.cfginference.core.event.EventListener;
import org.cfginference.core.event.EventManager;

public final class EnterAnalysis implements EventListener {

    private final Context context;

    private final GeneralContext generalContext;

    private final ASTAnalysisScanner astAnalysisScanner;

    private final CFGAnalysisScanner cfgAnalysisScanner;

    private final EventManager eventManager;

    private EnterAnalysis(Context context) {
        this.context = context;
        this.generalContext = GeneralContext.instance(context);
        this.astAnalysisScanner = ASTAnalysisScanner.instance(context);
        this.cfgAnalysisScanner = CFGAnalysisScanner.instance(context);
        this.eventManager = EventManager.instance(context);

        eventManager.register(this);
    }

    public static EnterAnalysis instance(Context context) {
        EnterAnalysis instance = context.get(EnterAnalysis.class);
        if (instance == null) {
            instance = new EnterAnalysis(context);
        }
        return instance;
    }

    @Override
    public void finished(Event e) {
        if (e == Event.SimpleEvent.FULL_ANALYSIS) {
            SolveConstraints.instance(context).start();
            AfterAnalysis.instance(context).start();
        }
    }

    public void enter(TreePath path) {
        generalContext.setRoot(path.getCompilationUnit());
        Event newAnalysisEvent = new Event.NewAnalysisTask(path.getCompilationUnit(), (ClassTree) path.getLeaf());
        eventManager.broadcast(newAnalysisEvent, true);

        cfgAnalysisScanner.scan(path, null);
        astAnalysisScanner.scan(path, null);

        eventManager.broadcast(newAnalysisEvent, false);
        generalContext.setRoot(null);
    }
}
