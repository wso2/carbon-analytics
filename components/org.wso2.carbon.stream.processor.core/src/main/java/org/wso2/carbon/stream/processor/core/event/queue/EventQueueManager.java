package org.wso2.carbon.stream.processor.core.event.queue;

import org.wso2.carbon.stream.processor.core.internal.SiddhiAppData;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.persistence.IncrementalDBPersistenceStore;
import org.wso2.siddhi.core.stream.input.source.Source;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventQueueManager {
    private static EventQueue<QueuedEvent> eventQueue;
    private IncrementalDBPersistenceStore incrementalDBPersistenceStore;

    public EventQueueManager() {
        incrementalDBPersistenceStore = new IncrementalDBPersistenceStore();
    }

    public static EventQueue<QueuedEvent> initializeEventQueue(int queueSize) {
        eventQueue = new EventQueue<>(queueSize);
        return eventQueue;
    }

    public static EventQueue getEventQueue() {
        return eventQueue;
    }

    public void trimAndSend() throws InterruptedException {

        Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.getStreamProcessorService().
                getSiddhiAppMap();

        Map<String,String> revisionForSiddhiAppMap = incrementalDBPersistenceStore.
                getAllLatestMapOfRevisionsForSiddhiAppsFromDB();

        for (Map.Entry<String, String> map : revisionForSiddhiAppMap.entrySet()) {
            long persistedTimestamp = Long.parseLong(map.getValue().split("_")[0]);
            for (Map.Entry<String, SiddhiAppData> entry : siddhiAppMap.entrySet()) {
                if(entry.getValue().getSiddhiApp().equals(map.getKey())){
                    Collection<List<Source>> sourceCollection = entry.getValue().getSiddhiAppRuntime().getSources();
                    for (List<Source> sources : sourceCollection) {
                        for (Source source : sources) {
                            Iterator<QueuedEvent> itr = eventQueue.getQueue().iterator();
                            while (itr.hasNext()) {
                                QueuedEvent queuedEvent = itr.next();
                                if(persistedTimestamp < queuedEvent.getEvent().getTimestamp()){
                                    source.getMapper().getHandler().sendEvent(queuedEvent.getEvent());
                                    eventQueue.getQueue().remove(queuedEvent);
                                }
                            }
                        }
                    }
                }
            }
        }
        eventQueue.getQueue().clear();
    }
}
