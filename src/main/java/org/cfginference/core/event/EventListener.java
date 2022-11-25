package org.cfginference.core.event;

public interface EventListener {
    default void started(Event e) {}

    default void finished(Event e) {}
}
