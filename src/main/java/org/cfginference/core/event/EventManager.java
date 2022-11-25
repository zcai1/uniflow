package org.cfginference.core.event;

import com.sun.tools.javac.util.Context;

import java.util.LinkedHashSet;
import java.util.Set;

public final class EventManager {

    private static final Context.Key<EventManager> contextKey = new Context.Key<>();

    private final Set<EventListener> listeners = new LinkedHashSet<>();

    private EventManager(Context context) {
        context.put(contextKey, this);
    }

    public static EventManager instance(Context context) {
        EventManager em = context.get(contextKey);
        if (em == null) {
            em = new EventManager(context);
        }
        return em;
    }

    public void register(EventListener listener) {
        listeners.add(listener);
    }

    public void unregister(EventListener listener) {
        listeners.remove(listener);
    }

    public void broadcast(Event e, boolean start) {
        if (start) {
            for (EventListener l : listeners) {
                l.started(e);
            }
        } else {
            for (EventListener l : listeners) {
                l.finished(e);
            }
        }
    }
}
