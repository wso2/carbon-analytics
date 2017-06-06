package util;

import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class StreamProcessorUtil implements EventStreamService {
    private HashMap<String, HashMap<String, List<Attribute>>> streamAttributesMap = new HashMap<>();
    private LinkedList<EventData> eventsReceived = new LinkedList<>();

    public StreamProcessorUtil() { }

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
    public List<Attribute> getStreamAttributes(String executionPlanName, String streamName)
            throws ResourceNotFoundException {
        if (streamAttributesMap.containsKey(executionPlanName)) {
            if (streamAttributesMap.get(executionPlanName).containsKey(streamName)) {
                return streamAttributesMap.get(executionPlanName).get(streamName);
            } else {
                throw new ResourceNotFoundException("Siddhi app '" + executionPlanName + "' does not contain " +
                        "stream '" + streamName + "'.", ResourceNotFoundException.ResourceType.STREAM_NAME,
                        streamName);
            }
        } else {
            throw new ResourceNotFoundException("Siddhi app '" + executionPlanName + "' does not exist.",
                    ResourceNotFoundException.ResourceType.SIDDHI_APP_NAME, executionPlanName);
        }
    }

    @Override
    public void pushEvent(String executionPlanName, String streamName, Event event) {
        eventsReceived.add(new EventData(executionPlanName, streamName, event));
    }

    public int getNoOfEvents() {
        return eventsReceived.size();
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

    public void resetEvents() {
        eventsReceived.clear();
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
