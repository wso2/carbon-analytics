package org.wso2.carbon.stream.processor.core.event.queue;

import org.wso2.carbon.stream.processor.core.ha.InputHandlerPersistInfo;
import org.wso2.carbon.stream.processor.core.internal.SiddhiAppData;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.siddhi.core.stream.input.source.Source;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventQueueManager {
    private static EventQueue<QueuedEvent> eventQueue;

    public static EventQueue<QueuedEvent> initializeEventQueue(int queueSize) {
        eventQueue = new EventQueue<>(queueSize);
        return eventQueue;
    }

    public static EventQueue getEventQueue() {
        return eventQueue;
    }

    public static void trimAndSend(List<InputHandlerPersistInfo> persistInfoList) throws InterruptedException {

        Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.getStreamProcessorService().
                getSiddhiAppMap();

        for (InputHandlerPersistInfo persistInfo : persistInfoList) {
            Iterator<QueuedEvent> itr = eventQueue.getQueue().iterator();
            while (itr.hasNext()) {
                QueuedEvent queuedEvent = itr.next();
                if(persistInfo.getSourceHandlerId() == queuedEvent.getSourceHandlerElementId()
                        && persistInfo.getTimestamp() < queuedEvent.getTimestamp()){
                    for (Map.Entry<String, SiddhiAppData> entry : siddhiAppMap.entrySet()) {
                        Collection<List<Source>> sourceCollection = entry.getValue().getSiddhiAppRuntime().getSources();
                        for (List<Source> sources : sourceCollection) {
                            for (Source source : sources) {
                                if(source.getMapper().getHandler().getElementId().equals(
                                        persistInfo.getSourceHandlerId())){
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
