package org.wso2.carbon.streaming.integrator.core.dropped.events.preservation;

import io.siddhi.core.exception.MappingFailedException;
import io.siddhi.core.util.preservation.PreservationStore;

import java.util.List;

public class DBPreservationStore implements PreservationStore { // TODO implement DB
    @Override
    public void saveTransportError(String siddhiAppName, String streamName, List<Object> failedEvents, Exception e) {
        System.out.println("===[Transport Error BEGIN]===========");
        System.out.println(String.format("Preserving Dropped Event in SiddhiApp: %s Stream: %s", siddhiAppName, streamName) + ". Cause: " + e.getMessage());
        for (Object failedEvent : failedEvents) {
            System.out.println(failedEvent.toString());
        }
        System.out.println("===[Transport Error END===========");
    }

    @Override
    public void saveMappingError(String siddhiAppName, String streamName, List<Object> failedEvents, MappingFailedException e) {
        System.out.println("===[Mapping Error BEGIN]===========");
        System.out.println(String.format("Preserving Dropped Event in SiddhiApp: %s Stream: %s", siddhiAppName, streamName) + ". Cause: " + e.getMessage());
        for (Object failedEvent : failedEvents) {
            System.out.println(failedEvent.toString());
        }
        System.out.println("===[Mapping Error END===========");
    }
}
