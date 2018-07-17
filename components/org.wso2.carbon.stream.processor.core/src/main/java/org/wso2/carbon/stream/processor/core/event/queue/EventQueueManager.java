package org.wso2.carbon.stream.processor.core.event.queue;

public class EventQueueManager {
    private static EventQueue<QueuedEvent> eventQueue;

    public static EventQueue<QueuedEvent> initializeEventQueue(int queueSize) {
        eventQueue = new EventQueue<>(queueSize);
        return eventQueue;
    }

    public static EventQueue getEventQueue() {
        return eventQueue;
    }
}
