package util;

import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class StreamProcessorUtil implements EventStreamService {
    private int noOfEvents;
    private HashMap<String, HashMap<String, List<Attribute>>> streamAttributesMap = new HashMap<>();
    private LinkedList<EventData> eventsReceived = new LinkedList<>();

    public StreamProcessorUtil(int noOfEvents) {
        this.noOfEvents = noOfEvents;
    }

    @Override
    public List<String> getStreamNames(String streamName) {
        List<String> streamNames = new ArrayList<>();
        streamAttributesMap.forEach((executionPlan, streamMap) -> {
            if (streamMap.containsKey(streamName)) {
                streamMap.get(streamName).forEach(attribute -> streamNames.add(attribute.getName()));
            }
        });
        return streamNames;
    }

    @Override
    public List<Attribute> getStreamAttributes(String executionPlanName, String streamName) {
        if (streamAttributesMap.containsKey(executionPlanName)) {
            if (streamAttributesMap.get(executionPlanName).containsKey(streamName)) {
                return streamAttributesMap.get(executionPlanName).get(streamName);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void pushEvent(String executionPlanName, String streamName, Event event) {
        noOfEvents++;
        eventsReceived.add(new EventData(executionPlanName, streamName, event));
    }

    public int getNoOfEvents() {
        return noOfEvents;
    }

    public void setNoOfEvents(int noOfEvents) {
        this.noOfEvents = noOfEvents;
    }

    public HashMap<String, HashMap<String, List<Attribute>>> getStreamAttributesMap() {
        return streamAttributesMap;
    }

    public void setStreamAttributesMap(HashMap<String, HashMap<String, List<Attribute>>> streamAttributesMap) {
        this.streamAttributesMap = streamAttributesMap;
    }

    public LinkedList<EventData> getEventsReceived() {
        return eventsReceived;
    }

    public void setEventsReceived(LinkedList<EventData> eventsReceived) {
        this.eventsReceived = eventsReceived;
    }

    public void addStreamAttributes(String executionPlanName, String streamName, List<Attribute> attributes) {
        if (streamAttributesMap.containsKey(executionPlanName)) {
            streamAttributesMap.get(executionPlanName).put(streamName, attributes);
        } else {
            streamAttributesMap.put(executionPlanName, new HashMap<String, List<Attribute>>() {
                {
                    put(streamName, attributes);
                }
            });
        }
    }
}
